(ns ais.resilient_decoder
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mapping.core :as ais-map])
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

(defn- consume [n in-ch]
  (take-while ais-util/not-nil? (repeatedly n #(async/<!! in-ch))))

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
        (let [n (m_fc (first unpaired-ret))]
          (if (= (+ (count unpaired-ret) (count frags)) n) ; all fragments present => match
            (do
              (swap! unpaired-frags dissoc grp-k) ; fragment set complete, remove key
              (recur (rest pairs) (conj result (sort-by m_fn (concat frags unpaired-ret)))))
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

(defn- complete-multipart? 
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

(defn- -forward [source include-types out-ch]
  (loop [msgs source
         acc []]
    (if-let [msg-frags (first msgs)]
      (if (contains? include-types (m_type (first msg-frags))) 
        (do
          (async/>!! out-ch msg-frags)
          (recur (rest msgs) acc))
        (recur (rest msgs) (concat acc msg-frags)))
      acc)))

(defn- -forward-multipart [include-types unpaired-frags groups out-ch]
  (let [notag (groups :notag)]
    (if-let [lead-frag (first notag)]
      ;; ASSUMPTION: multipart fragments are either tagged or untagged (and not both) in an input stream.
      ;; In either case, determine if input fragment sequence forms a complete message.  In tagged
      ;; case, we can search for unmatched fragments in cache to form complete message.  In untagged
      ;; case, no searches can be done; As such, if current fragment sequence doesn't form a complete
      ;; message they cannot be used as possible matches for subsequent fragment sequences due to ordering 
      ;; ambiguity 
      (if (and (complete-multipart? (m_fc lead-frag) notag) (contains? include-types (m_type lead-frag)))
        (do
          (async/>!! out-ch notag)
          [])
        notag)
      (-forward (parse-multipart (dissoc groups :notag :single) unpaired-frags) include-types out-ch))))

(defn- preprocess-multipart [line include-types unpaired-frags in-ch out-ch]
  (let [remaining (consume (- (m_fc line) 1) in-ch)
        groups (group-fragments (conj (filter valid-syntax? remaining) line))]
    {:drop (concat (-forward (map vector (groups :single)) include-types out-ch)
                   (-forward-multipart include-types unpaired-frags groups out-ch)
                   (filter (complement valid-syntax?) remaining))}))

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
        unpaired-frags (atom {})]
    (async/thread
      (loop [dropped 0
             invalid 0]
        (if-let [line (async/<!! in-ch)]
          (if (valid-syntax? line) 
            (if (= (m_fn line) 1) ; start message (single | multipart)
              (if (= (m_fc line) 1)       
                (if (contains? include-types (m_type line))
                  (do
                    (async/>!! out-ch [line])
                    (recur dropped invalid))
                  (recur (inc dropped) invalid))
                (let [ret (preprocess-multipart line include-types unpaired-frags in-ch out-ch)]
                    (recur (+ dropped (count (ret :drop))) invalid)))
              (let [ret (preprocess-multipart line include-types unpaired-frags in-ch out-ch)]
                  (recur (+ dropped (count (ret :drop))) invalid)))
            (do
              (logging/debug (format "Invalid message syntax: %s" line))
              (recur dropped (inc invalid))))
          (do
            (log-metrics (+ dropped (count @unpaired-frags)) invalid)
            (async/close! out-ch)))))
    out-ch)) 
               
(defn- decode [format & msgs]
  (try
    (apply ais-core/parse-ais format (apply ais-core/verify msgs))
    (catch Exception e
      (logging/error e)
      (logging/error msgs))))

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
