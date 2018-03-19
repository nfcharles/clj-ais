(ns ais.runner
  (:require [ais.util :as ais-util]
            [ais.extractors :as ais-ex]
            [ais.vocab :as ais-vocab]
            [ais.mapping.core :as ais-map]
            [ais.core :as ais-core]
            [clojure.core.async :as async]
            [clojure.data.csv :as csv]
            [clojure.pprint :as pprint]
            [clojure.stacktrace :as strace]
            [clj-json.core :as json]
            [taoensso.timbre :as logging])
  (:gen-class))


;;; NOTE: Multipart sentences that are out of order are dropped.
;;; TODO: Send to dedicated output channel for reconciliation.

(def buffer-size 100)

;;---
;; Writers
;; ---

(defn json-write [writer data ]
  (.write writer (json/generate-string data)))

(defn csv-write [writer data]
  (csv/write-csv writer data))

(def writer-factory (hash-map
 "json" json-write
 "csv"  csv-write))

(defn- write [filename out-format lines]
  (with-open [writer (clojure.java.io/writer filename)]
    ((writer-factory out-format) writer lines)))

;;---
;; Core
;;---

(defn parse-types [types]
  (into #{} (map read-string (clojure.string/split types #","))))

(defn- consume [base n ch]
  (loop [i n
         acc [base]]
    (if (> i 0)
      (if-let [line (async/<!! ch)]
        (recur (dec i) (conj acc (ais-ex/tokenize line)))
	acc)
      acc)))

(defn <<multipart [msg ch]
  (consume msg (- (msg :fc) 1) ch))

(defn- log-metrics [dropped invalid error histogram]
  (logging/info (format "count.dropped.total=%d" dropped))
  (logging/info (format "count.invalid.total=%d" invalid))
  (logging/info (format "count.error.total=%d" error))
  (doseq [[k v] (sort histogram)]
    (logging/info (format "count.dropped.type-%d=%d" k v))))

(defn- filter-stream [include-types in-ch]
  (let [out-ch (async/chan buffer-size)]
    (async/thread
      (loop [dropped 0
             invalid 0
	     error   0
	     hist (transient [])]
	(if-let [line (async/<!! in-ch)]
          (if-let [msg (ais-ex/tokenize line)]
            (if (= (msg :fn) 1)
              (if (contains? include-types (msg :ty))
	        (let [chksum (ais-util/checksum (msg :en))
		      ^long frag-count (msg :fc)]
                  ;; The current sentence syntax has been verified and is a starting
                  ;; fragment; if it is part of a multipart sentence consume the remaining
                  ;; fragments otherwise process as a complete standalone senetence.
                  (if (= chksum (msg :ck))
                    ;; Checksum verified
                    (if (= frag-count 1)
                      ;; *** Single fragment sentence ***
                      (do
                        (async/>!! out-ch [msg])
                        (recur dropped invalid error hist))
                      ;; *** Multipart sentence, consume remaining fragments ***
                      (let [msgs (<<multipart msg in-ch)]
                        (async/>!! out-ch msgs)
                        (recur dropped invalid error hist)))
                    ;; Checksum failed verification
                    (do
                      (logging/error
                        (format "Invalid message checksum %s. [expected]%s != [actual]%s" (msg :en) chksum (msg :ck)))
                      (if (= frag-count 1)
                        (recur dropped (inc invalid) error hist)
			(do
			  (<<multipart msg in-ch)
                          (recur (+ dropped frag-count) invalid error hist))))))
                (let [^long frag-count (msg :fc)]
                  ;; *** Skip messages ***
                  (if (= (msg :fc) 1)
                    (recur (inc dropped) invalid error (conj! hist (msg :ty)))
                    (do
                      ;(println (format "Skipping %s sentences" (msg :fc)))
		      (<<multipart msg in-ch)
                      (recur (+ dropped frag-count) invalid error (conj! hist (msg :ty)))))))
	      (do
                (logging/warn (format "Multipart fragment received out or order - %s" line))
                (recur dropped (inc invalid) (inc error) hist)))
            (do
              ;(logging/error (format "Error parsing message: %s" line))
              (recur dropped (inc invalid) (inc error) hist)))
          (do
            (log-metrics dropped invalid error (frequencies (persistent! hist)))
            (async/close! out-ch)))))
    out-ch))

(defn decode [out-format fragments]
  (try
    (ais-core/parse out-format fragments)
    (catch Exception e
      (logging/error e)
      (logging/error fragments))))

(defn- process [out-format n-threads buffer-len in-ch]
  (let [out-ch (async/chan buffer-size)
        active-threads (atom n-threads)]
    (dotimes [i n-threads]
      (async/thread
        (loop [j 0
	       err 0
	       total 0
	       acc (transient [])]
          (if-let [fragments (async/<!! in-ch)]
	    (if-let [msg (decode out-format fragments)]
	      (if (< j buffer-len)
                (recur (inc j) err total (conj! acc msg))
		(do
	          (async/>!! out-ch (persistent! acc))
		  (recur 0 err (+ j total) (transient []))))
              (recur j (inc err) total acc))
            (do
	      (if (> j 0)
	        (async/>!! out-ch (persistent! acc)))
              (logging/info (format "count.decoder.thread_%d=%d" i (+ j total)))
              (logging/info (format "count.decoder.err.thread_%d=%d" i err)))))
        (swap! active-threads dec)
        (if (= @active-threads 0)
          (async/close! out-ch))))
    out-ch))


(defn -write [name out-format data thread-no]
  (logging/info (format "writing %s" name))
  (write name out-format data)
  (logging/info (format "count.writer.thread_%s=%s" thread-no (count data))))

(defn- writer [out-format prefix n in-ch]
  (let [out-ch (async/chan)
        active-threads (atom n)
	;; <prefix>-part-<thread-n>-<batch-n>.<suffix>
        filename #(format "%s-part-%d-%d.%s" %1 %2 %3 %4)]
    (dotimes [i n]
      (async/thread
        (loop [j 0]
          (if-let [msgs (async/<!! in-ch)]
            (do
              (-write (filename prefix i j out-format) out-format msgs i)
	      (recur (inc j)))
	    (do
              (swap! active-threads dec)
              (when (= @active-threads 0)
                (async/>!! out-ch :done)
                (async/close! out-ch)))))))
    out-ch))

(defn run [in-ch out-prefix include-types n-threads out-format buffer-len]
  (let [out-ch (->> (filter-stream include-types in-ch)
                    (process out-format n-threads buffer-len))]
    ;; Thread macro uses daemon threads so we must explicitly block
    ;; until all writer threads are complete to prevent premature
    ;; termination of main thread.
    (async/<!! (writer out-format out-prefix n-threads out-ch))))
