(ns ais.resilient_decoder
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mappings :as ais-map])
  (:require [ais.core :as ais-core])
  (:require [clojure.core.async :as async])
  (:require [clojure.data.json :as json])
  (:require [clojure.data.csv :as csv])
  (:require [clojure.pprint :as pprint])
  (:require [clojure.stacktrace :as strace])
  (:require [taoensso.timbre :as logging])
  (:require [taoensso.timbre.appenders.core :as appenders])
  (:gen-class))

;;;                           ____                     _           
;;;                          |  _ \  ___  ___ ___   __| | ___ _ __ 
;;;                          | | | |/ _ \/ __/ _ \ / _` |/ _ \ '__|
;;;                          | |_| |  __/ (_| (_) | (_| |  __/ |   
;;;                          |____/ \___|\___\___/ \__,_|\___|_|   
;;;
;;; The following application is a robust implemenation of ais-lib's core decoding functionality.  In practice, multipart 
;;; messages tend to stream in correct monotonically increasing order however we must account for all streaming permutations
;;; and not naively consume input streams.
;;; 
;;; Consider two sets or multipart messages, [a, a'] and [b, b']. x+ denotes 1 more messages.
;;;
;;; We must consider the following senarios when parsing multipart fragments from input streams:
;;;
;;; 1. a  a'
;;; 2. a' a
;;; 3. a  x+ a'
;;; 4. a' x+ a
;;; 5. a  b' b a'
;;;
;;;
;;; Entry point parameters:
;;;  * output-prefix: string
;;;    - Output filename prefix
;;;  * include-types: [1-28]
;;;    - Messages types to decode.  All other messages are dropped
;;;  * n-threads: [1..)
;;;    - Number of processing threads.  Also sets the number of output files (where output files have the suffix
;;;      part-n).
;;;  * format: [csv | json]
;;;    - Output file data format
;;;


(def buffer-size 1000) 

;;---
;; Writers
;; ---

(defmulti write-data 
  (fn [output-format & _] output-format))

(defmethod write-data "json" [_ writer data ]
  (.write writer (json/write-str data)))

(defmethod write-data "csv" [_ writer data]
  (csv/write-csv writer data))

(defmethod write-data :default [_ writer data]
  (write-data "csv" writer data))

(defn- write [filename output-format lines]
  (with-open [writer (clojure.java.io/writer filename)]
    (write-data output-format writer lines)))

;;---
;; Util
;;--
  
(defn- parse-types [types]
  (into #{} (map read-string (clojure.string/split types #","))))

(defn- extract-type [payload]
  (->> (seq payload)
       (first)
       (ais-util/char->decimal)))

(defn- valid-syntax? [message]
  (== (count (ais-ex/parse "env-chksum" message)) 2))

(defn- m_type [line]
  (extract-type (ais-ex/parse "payload" line)))

(defn- m_fc [line]
  (ais-ex/parse "frag-count" line))

(defn- m_fn [line]
  (ais-ex/parse "frag-num" line))  



;;---
;; Core
;;---

; filter nils
(defn- consume [n in-ch]
  (filter valid-syntax? (take-while ais-util/not-nil? (repeatedly n #(async/<!! in-ch)))))

(defn- group-key [line]
  (if-let  [prefix (ais-ex/parse "g" line)]
    (let [key (format "%s-%s" prefix (ais-ex/parse "radio-ch" line))]
      key)))

(defn- group-fragments 
  "Groups fragments into at least 2 categories:
  
    :single   : single message
    :notag    : multipart message fragments with no tag block
    (grp-key) : tagged multipart message fragments
  "
  [lines]
  (loop [acc {:single [] :notag []}
         msgs lines]
    (if-let [msg (first msgs)]
      (if-let [key (group-key msg)]
        (recur (update acc key conj msg) (rest msgs))
        (recur (update acc (if (= (m_fc msg) 1) :single :notag) conj msg) (rest msgs)))
      acc)))

(defn- find-matches!
  "For input (frag-group-key, [frag-1 .. frag-n]) tuple, look up remaining group members
  in unpaired frags.  If set complete, sort fragments and add to result set."
  [frag-groups unpaired-frags]
  (loop [pairs (seq frag-groups)
         result []]
    (if-let [[grp-k frags] (first pairs)]
      (if-let [unpaired-ret (@unpaired-frags grp-k)]
        (let [n (ais-ex/parse "frag-count" (first unpaired-ret))]
          ;; |frags| + |unpaired-ret| == n --> complete fragment set
          (if (= (+ (count unpaired-ret) (count frags)) n)
            (do
              (swap! unpaired-frags dissoc grp-k) ; fragment set complete, remove key
              (recur (rest pairs) (conj result (sort-by (partial ais-ex/parse "frag-num") (concat frags unpaired-ret)))))
            (do
              (swap! unpaired-frags update grp-k concat frags)
              (recur (rest pairs) result))))
        (do
          (swap! unpaired-frags update grp-k concat frags)
          (recur (rest pairs) result)))
      result)))
          
(defn- assemble-multipart 
  "Returns a complete multipart message if possible, sorted by fragment number ASC,
  nil otherwise."
  [frag-groups]
  (if (= 1 (count frag-groups))
    (let [frags (first (vals frag-groups))]
      (if (and (not-any? nil? frags) 
               (= (m_fc (first frags)) (count frags)))
        (sort-by m_fn frags)))))

(defn complete-multipart? 
  "Determines if an input sequence of message fragments is monotonic -- using fragment
  numbers as indicies.  If this is true and the sequence cardinality matches n, it connotes 
  the sequence is a complete multipart message.  Useful if sequence doesn't have group tags.  
  Determining set membership is trivial when fragments have group tags, e.g 'g-1-2-5, g-2-2-5'."
  [n msgs]
  (and 
    (= (map m_fn msgs) (range 1 (inc (count msgs))))
    (= (map m_fc msgs) (repeat (count msgs) n))))
      
(defn- parse-multipart [frag-groups unpaired-frags]
  (if-let [frag-set (assemble-multipart frag-groups)]
    (list frag-set)
    (for [match (find-matches! frag-groups unpaired-frags)]
      match)))

(defn- _decode? [include-types line]
  (if (= (m_fn line) 1)
    (contains? include-types (m_type line)) true))

(defn- preprocess-multipart [line include-types unpaired-frags in-ch out-ch]
  (let [remaining (filter (partial _decode? include-types) (consume (- (m_fc line) 1) in-ch))
        lines (conj remaining line)
        groups (group-fragments lines)]
    (doseq [msg (groups :single)]
      ;; Send along any single messages that are interspersed in multipart fragments --
      ;; possible via fragment reordering.
      (async/>!! out-ch [msg]))
    (let [notag (groups :notag)]
      (if-let [frag (first notag)]
        ;; Assumption: multipart fragments are either tagged or untagged and not both.
        (if (and (> (count notag) 0) (complete-multipart? (m_fc frag) notag) (contains? include-types (m_type frag)))
          (do
            (async/>!! out-ch notag)
            {:notag []})
          {:notag notag})
        (do
          (doseq [msg (parse-multipart (dissoc groups :notag :single) unpaired-frags)]
            ;; We can't verify multipart message types until entire message is constructed.
            ;; Only propagate multiparts in include list
            (if (contains? include-types (m_type (first msg)))
              (async/>!! out-ch msg)))
          {:notag notag})))))

(defn- log-metrics [dropped invalid]
  (logging/info (format "count.invalid.total=%d" invalid))
  (logging/info (format "count.dropped.total=%d" dropped)))

;;;
;;; --- PROCESS PIPELINE FUNCTIONS
;;;
;;; (1) filter-stream
;;;   Preprocesses input stream of messages, forwarding only messages specified in include-types.
;;;   Multipart message fragments are appropriately assembled.  Incomplete messages are dropped.
;;;
;;;   TODO: Collect unmatched multipart fragments and write to disk.
;;;  
;;; (2) process
;;;   Decodes messages -- single and multipart.
;;;
;;; (3) collect
;;;   Accumulates decoded messages and prepares for writing.
;;;
;;; (4) writer
;;;   Writes decoded messages to disk.
;;;

(defn- filter-stream [include-types in-ch]
  (let [out-ch (async/chan buffer-size)
        decode? (partial _decode? include-types)
        unpaired-frags (atom {})]
    (async/thread
      (loop [dropped 0
             invalid 0]
        (if-let [line (async/<!! in-ch)]
          (if (valid-syntax? line) 
            (if (decode? line)
              (if (= (m_fn line) 1) ; start message (single | multipart)
                (if (= (m_fc line) 1)       
                  (do
                    (async/>!! out-ch [line])
                    (recur dropped invalid))
                  (let [groups (preprocess-multipart line include-types unpaired-frags in-ch out-ch)]
                    (recur (+ dropped (count (groups :notag))) invalid)))
                (let [groups (preprocess-multipart line include-types unpaired-frags in-ch out-ch)]
                  (recur (+ dropped (count (groups :notag))) invalid)))
              (recur (inc dropped) invalid))
            (do
              (logging/debug (format "Invalid message syntax: %s" line))
              (recur dropped (inc invalid))))
          (do
            (log-metrics dropped invalid)
            (async/close! out-ch)))))
    out-ch)) 

(defn- decode [format & msgs]
  (try
    (apply ais-core/parse-ais format (apply ais-core/verify msgs))
    (catch Exception e
      (pprint/pprint msgs)
      (logging/error e))))

(defn- process [format n in-ch]
  (let [out-ch (async/chan buffer-size)
        active-threads (atom n)]
    (dotimes [i n]
      (async/thread
        (loop [acc []]
          (if-let [msgs (async/<!! in-ch)]
            (if-let [decoded (apply decode format msgs)]
              (recur (conj acc decoded))
              (recur acc))
            (do
              (logging/info (str "count.decoder.thread_" i "=" (count acc)))
              (async/>!! out-ch acc))))
        (swap! active-threads dec)
        (if (= @active-threads 0)
          (async/close! out-ch))))
    out-ch))

(defn- collect [in-ch]
  (let [out-ch (async/chan)]
    (async/thread
      (loop [acc []]
        (if-let [batch (async/<!! in-ch)]
          (do
            (logging/info (str "count.collector=" (count batch))) 
            (recur (conj acc batch)))
          (async/>!! out-ch acc))))
    out-ch))

(defn- writer [output-format prefix batches]
  (let [out-ch (async/chan)
        n (count batches)
        active-threads (atom n)
        filename #(format "%s-part-%s.%s" %1 %2 %3)]
    (doseq [[i batch] (map list (range n) batches)]
      (async/thread
        (logging/info (format "writing %s" (filename prefix i output-format)))
        (write (filename prefix i output-format) output-format batch)
        (logging/info (format "count.writer.thread_%s=%s" i (count batch)))
        (swap! active-threads dec)
        (when (= @active-threads 0)
          (async/>!! out-ch :done)
          (async/close! out-ch))))
    out-ch))

(defn run [in-ch output-prefix include-types nthreads output-format]
  (let [out-ch (->> (filter-stream include-types in-ch)
                    (process output-format nthreads)
                    (collect))]
    ;; Thread macro uses daemon threads so we must explicitly block 
    ;; until all writer threads are complete to prevent premature
    ;; termination of main thread.
    (async/<!! (writer output-format output-prefix (async/<!! out-ch)))))

(def stdin-reader
  (java.io.BufferedReader. *in*))


(defn configure-logging [log-stream]
  (logging/merge-config!
    {:appenders {
      :println (appenders/println-appender {:stream log-stream})}}))

(defn -main
  [& args]
  (try
    (let [output-prefix (nth args 0)
          include-types (parse-types (nth args 1))
          nthreads (Integer. (nth args 2))
          output-format (nth args 3)
          stream (async/to-chan (line-seq stdin-reader))]
      (println (format "INCLUDE TYPES: %s" include-types))
      (configure-logging :std-err)
      (time (run stream output-prefix include-types nthreads output-format)))
    (catch Exception e
      (logging/fatal e))))
