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
;;;  * supported-types: [1-28]
;;;    - Messages types to decode.  All other messages are dropped
;;;  * n-threads: [1..)
;;;    - Number of processing threads.  Also sets the number of output files (where output files have the suffix
;;;      part-n).
;;;  * format: [csv | json]
;;;    - Output file data format
;;;


(def buffer-size 1000) 
(def ais-msg-types (into {} (for [k (range 1 28)] [k false])))

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

(defn- merge-types [types]
  (apply merge ais-msg-types types))

(defn- parse-types [types]
  (into {} (for [k (clojure.string/split types #",")] [(read-string k) true])))

(defn- extract-type [payload]
  (->> (seq payload)
       (first)
       (ais-util/char->decimal)))

(defn valid-syntax? [message]
  (== (count (ais-ex/parse "env-chksum" message)) 2))

;;---
;; Core
;;---

(defn consume [n in-ch]
  (repeatedly n #(async/<!! in-ch)))

(defn meta-fields [line]
  (hash-map
    "frag-count" (ais-ex/parse "frag-count" line)
    "frag-num"   (ais-ex/parse "frag-num" line)
    "type"       (extract-type (ais-ex/parse "payload" line))))

(defn group-key [line]
  (if-let  [prefix (ais-ex/parse "g" line)]
    (let [key (format "%s-%s" prefix (ais-ex/parse "radio-ch" line))]
      key)))

(defn group-fragments [lines]
  (loop [acc {:send [] :drop []}
         msgs lines]
    (if-let [msg (first msgs)]
      (let [key (group-key msg)]
        (if (nil? key)
          (if (> (ais-ex/parse "frag-count" msg) 1)
            (recur (update acc :drop conj msg) (rest msgs))   ; Drop untagged multipart fragments
            (recur (update acc :send conj msg) (rest msgs)))  ; Send single messages
          (recur (update acc key conj msg) (rest msgs))))     ; Update grouping
      acc)))

(defn assemble-multipart [frag-groups]
  ;; Return complete multipart message if possible, sorted by fragment number
  (if (= 1 (count frag-groups))
    (let [frags (first (vals frag-groups))]
      (if (and (not-any? nil? frags) 
               (= (ais-ex/parse "frag-count" (first frags)) (count frags)))
        (sort-by (partial ais-ex/parse "frag-num") frags)))))

(defn find-matches! [frag-groups unpaired-frags]
  ;; For input (frag-group-key, [frag-1 .. frag-n]) tuple, look up remaining group members
  ;; in unpaired frags.  If set complete, sort fragments and add to result set.
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
      
(defn send-multipart [frag-groups unpaired-frags out-ch]
  (if-let [frag-set (assemble-multipart frag-groups)]
    (async/>!! out-ch frag-set)
    (doseq [match (find-matches! frag-groups unpaired-frags)]
      (async/>!! out-ch match))))

(defn passthru? [supported-types m]
  (or 
    (supported-types (m "type"))
    (and (> (m "frag-num") 1) (> (m "frag-count") 1))))

(defn log-metrics [dropped invalid]
  (logging/info (format "count.invalid.total=%d" invalid))
  (logging/info (format "count.dropped.total=%d" dropped)))

(defn- filter-stream [supported-types in-ch]
  (let [out-ch (async/chan buffer-size)
        unpaired-frags (atom {})]
    (async/thread
      (loop [dropped 0
             invalid 0]
        (if-let [line (async/<!! in-ch)]
          (if (valid-syntax? line)
            (let [m (meta-fields line)]
              (if (passthru? supported-types m)
                (if (= (m "frag-count") 1)
                  (do
                    (async/>!! out-ch [line])
                    (recur dropped invalid))
                  (if (nil? (group-key line)) 
                    (recur (inc dropped) invalid) ; untagged multipart fragment -- can't deterministically group
                    (let [lines (conj (consume (- (m "frag-count") 1) in-ch) line)
                          all-groups (group-fragments lines)
                          frag-groups (dissoc all-groups :drop :send)]
                      (doseq [msg (all-groups :send)]
                        (async/>!! out-ch [msg]))
                      (send-multipart frag-groups unpaired-frags out-ch)
                      (recur (+ dropped (count (all-groups :drop))) invalid))))
                (recur (inc dropped) invalid)))
            (do
              (logging/debug (format "Invalid message syntax: %s" line))
              (recur dropped (inc invalid))))
          (do
            (log-metrics dropped invalid)
            (async/close! out-ch)))))
    out-ch))

(defn decode [format & msgs]
  (try
    (apply ais-core/parse-ais format (apply ais-core/verify msgs))
    (catch Exception e
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

(defn run [in-ch output-prefix supported-types nthreads output-format]
  (let [out-ch (->> (filter-stream supported-types in-ch)
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


;; TODO: "strict"    : "g" tags necessary for multipart messages
;;       "nonstrict" : non tagged multipart messages are assumed to stream sequentially

(defn -main
  [& args]
  (try
    (let [output-prefix (nth args 0)
          supported-types (merge-types (parse-types (nth args 1)))
          nthreads (Integer. (nth args 2))
          output-format (nth args 3)
          stream (async/to-chan (line-seq stdin-reader))]
      (configure-logging :std-err)
      (time (run stream output-prefix supported-types nthreads output-format)))
    (catch Exception e
      (logging/fatal e))))
