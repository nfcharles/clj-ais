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

;;;; Resilient message decoding

;; Single fragment decoding -- trivial case
;;  * forward to processing pipeline

;; Multi fragment decoding -- nontrivial
;;
;; Multiple cases -- each depending on sequencing order
;;
;; 1. A - B
;;  * messages stream back-to-back in correct ordinal positions
;;
;; 2. B - A
;;  * messages stream back-to-back in reversed ordinal positions
;;
;; 3. A - X+ - B
;;  * messages stream in correct ordinal positions separated by + messages
;;
;; 4. B - X+ - A
;;  * messages stream in reversed ordinal positions separated by + messages
;;
;;

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
    "line"       line
    "frag-count" (ais-ex/parse "frag-count" line)
    "frag-num"   (ais-ex/parse "frag-num" line)
    "type"       (extract-type (ais-ex/parse "payload" line))))

(defn group-key [line]
  (if-let  [prefix (ais-ex/parse "g" line)]
    (let [key (format "%s-%s" prefix (ais-ex/parse "radio-ch" line))]
      (println (format "group key: %s" key))
      key)))

(defn find-match [key fragments]
  (if-let [frag (@fragments key)]
    frag))

(defn find-fragment-match [out-ch group-key line unpaired-frags]
  (if-let [ret (find-match group-key unpaired-frags)]
    (async/>!! out-ch (sort-by (partial ais-ex/parse "frag-num") [line ret]))
    (swap! unpaired-frags assoc group-key line)))

(defn consume-multipart [base candidate out-ch unpaired-frags]
  (if (nil? candidate)
    ;; Find match for base in unpaired set
    (find-fragment-match out-ch (group-key (base "line")) (base "line") unpaired-frags)
    (let [frag-num   (ais-ex/parse "frag-num" candidate)
          frag-count (ais-ex/parse "frag-count" candidate)
          candidate-group-key  (group-key candidate)
          base-group-key (group-key (base "line"))]
      (if (nil? candidate-group-key)
        ;; Candidate message is not a multipart fragment.  Send candidate
        ;; to out channel and find possible match for base in unpaired set.  
        ;; If match cannot be found, add base to unpaired fragments.

        (do
          (println (format "candidate-group-key nil: %s %s" base-group-key candidate-group-key))
          (async/>!! out-ch [candidate])
          (find-fragment-match out-ch base-group-key (base "line") unpaired-frags)
          (pprint/pprint @unpaired-frags))

        ;; Candidate message is a multipart fragment.  Determine if candidate
        ;; and base fragment are a pair.  If yes, send to out channel, otherwise
        ;; find match for base in unpaired and add candidate to unpaired set. If
        ;; match found, send pair to out channel otherwise add base to unpaired.

        (if (= base-group-key candidate-group-key)
          (do
            (println (format "group key match: %s %s" base-group-key candidate-group-key))
            (async/>!! out-ch (sort-by (partial ais-ex/parse "frag-num") [(base "line") candidate])))
          (do
            (println (format "group key doesn't match: %s %s" base-group-key candidate-group-key))
            ;;(swap! unpaired-frags assoc candidate-group-key candidate)
            (find-fragment-match out-ch candidate-group-key candidate unpaired-frags)
            (find-fragment-match out-ch base-group-key (base "line") unpaired-frags)
            (pprint/pprint @unpaired-frags)))
      ))))

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
              (println (format "filter-stream: %s" line))
              (let [d (details line)]
                ;; Fragment 2 of multipart messages should be automatically passed thru. 
                (if (passthru? supported-types d)
	          (condp = (d "frag-count")
	            1 (do
                        (println "Consume 1")
			(println line)
                        (async/>!! out-ch [line]))
	            2 (do
                        (println (format "Consume 2: %s" (group-key line)))
			(println line)
                        (consume-multipart d (async/<!! in-ch) out-ch unpaired-frags))
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