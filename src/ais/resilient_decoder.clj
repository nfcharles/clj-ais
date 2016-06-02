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
  (:gen-class))

;;;                      ____                     _           
;;;                     |  _ \  ___  ___ ___   __| | ___ _ __ 
;;;                     | | | |/ _ \/ __/ _ \ / _` |/ _ \ '__|
;;;                     | |_| |  __/ (_| (_) | (_| |  __/ |   
;;;                     |____/ \___|\___\___/ \__,_|\___|_|   
;;;
;;; The following application is a robust implemenation of ais-lib's core decoding functionality.  In practice, multipart 
;;; messages tend to stream in correct monotonically increasing order however we must account for all streaming configurations
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
  (fn [format & _] format))

(defmethod write-data "json" [_ writer data ]
  (.write writer (json/write-str data)))

(defmethod write-data "csv" [_ writer data]
  (csv/write-csv writer data))

(defmethod write-data :default [_ writer data]
  (write-data "csv" writer data))

(defn- write [name format lines]
  (let [filename (str  name "." format)]
    (println (str "writing " filename))
    (with-open [writer (clojure.java.io/writer filename)]
      (write-data format writer lines))))

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

(defn details [line]
  (hash-map
    "frag-count" (ais-ex/parse "frag-count" line)
    "frag-num"   (ais-ex/parse "frag-num" line)
    "type"       (extract-type (ais-ex/parse "payload" line))))

(defn group-key [line]
  (if-let  [prefix (ais-ex/parse "g" line)]
    (let [key (format "%s-%s" prefix (ais-ex/parse "radio-ch" line))]
      key)))

(defn find-fragment-match! [out-ch group-key msg unpaired-frags]
  (if-let [frag-match (@unpaired-frags group-key)]
    (async/>!! out-ch (sort-by (partial ais-ex/parse "frag-num") [msg frag-match]))
    (swap! unpaired-frags assoc group-key msg)))

(defn consume-multipart [a-msg b-msg unpaired-frags out-ch]
  (if (nil? b-msg)
    ;; Clearly msg b is not a match, let's look up a possible match in unpaired fragments
    (find-fragment-match! out-ch (group-key a-msg) a-msg unpaired-frags)
    (let [a-key (group-key a-msg)
          b-key (group-key b-msg)]
      (if (nil? b-key)

        ;; Msg b is not a multipart fragment.  Send to out channel for processing
        ;; and find possible match for msg a in unpaired fragments.  If a match is
        ;; not found, add to unpaired fragments
        (do
          (async/>!! out-ch [b-msg])
          (find-fragment-match! out-ch a-key a-msg unpaired-frags))

        ;; Msg b is a multipart fragment; if grouping key matches msg a, send pair
        ;; to out channel otherwise find possible matches for both msgs in unparied
        ;; fragments.  Add msgs to unpaired fragments if matches not found.
        (if (= a-key b-key)
          (async/>!! out-ch (sort-by (partial ais-ex/parse "frag-num") [a-msg b-msg]))
          (do
            (find-fragment-match! out-ch b-key b-msg unpaired-frags)
            (find-fragment-match! out-ch a-key a-msg unpaired-frags)))))))

(defn passthru? [supported-types d]
  (or 
    (supported-types (d "type"))
    (and (= (d "frag-num") 2) (= (d "frag-count") 2))))

(defn decode [format & msgs]
  (try
    (apply ais-core/parse-ais format (apply ais-core/verify msgs))
    (catch Exception e
      (strace/print-stack-trace e)
      "DECODE-FAILED")))

(defn- filter-stream [supported-types in-ch]
  (let [out-ch (async/chan buffer-size)
        unpaired-frags (atom {})]
    (async/thread
      (loop []
        (if-let [line (async/<!! in-ch)]
          (do 
            (when (valid-syntax? line)
              (let [d (details line)]
                (if (passthru? supported-types d)
	          (condp = (d "frag-count")
	            1 (async/>!! out-ch [line])
	            2 (consume-multipart line (async/<!! in-ch) unpaired-frags out-ch)
	     	    (.println *err* (str "Unexpected fragment count: " (d "frag-count") ". " line))) 
	          (.println *err* (str "Dropping [type=" (d "type") "] " line))))) 
            (recur))
          (async/close! out-ch))))
    out-ch))

(defn- process [format n in-ch]
  (let [out-ch (async/chan buffer-size)
        active-threads (atom n)]
    (dotimes [i n]
      (println (str "thread-" i))
      (async/thread
        (loop [acc []]
          (if-let [msgs (async/<!! in-ch)]
            (recur (conj acc (apply decode format msgs)))
            (async/>!! out-ch acc)))
        (swap! active-threads dec)
        (if (= @active-threads 0)
          (async/close! out-ch))))
    out-ch))

(defn- collect [in-ch]
  (let [out-ch (async/chan)]
    (async/thread
      (loop [acc []]
        (if-let [msg (async/<!! in-ch)]
          (recur (conj acc msg))
          (async/>!! out-ch acc))))
    out-ch))

(defn- writer [format prefix msgs]
  (let [out-ch (async/chan)
        n (count msgs)
        active-threads (atom n)]
    (doseq [[i batch] (map list (range n) msgs)]
      (println (str "write thread-" i))
      (async/thread
        (write (str prefix "-part-" i) format batch)
        (swap! active-threads dec)
        (when (= @active-threads 0)
          (async/>!! out-ch :done)
          (async/close! out-ch))))
    out-ch))

(defn run [in-ch output-prefix supported-types nthreads format]
  (let [out-ch (->> (filter-stream supported-types in-ch)
                    (process format nthreads)
                    (collect))]
    ;; Thread macro uses daemon threads so we must explicitly block 
    ;; until all writer threads are complete to prevent premature
    ;; termination of main thread.
    (async/<!! (writer format output-prefix (async/<!! out-ch)))))

(def stdin-reader
  (java.io.BufferedReader. *in*))

(defn -main
  [& args]
  (let [output-prefix (nth args 0)
        supported-types (merge-types (parse-types (nth args 1)))
        nthreads (Integer. (nth args 2))
	format (nth args 3)
        stream (async/to-chan (line-seq stdin-reader))]
    (do
      (time (run stream output-prefix supported-types nthreads format)))))