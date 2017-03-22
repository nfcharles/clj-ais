(ns ais.decode
  (:require [ais.util :as ais-util]
            [ais.extractors :as ais-ex]
            [ais.vocab :as ais-vocab]
            [ais.mapping.core :as ais-map]
            [ais.core :as ais-core]
            [clojure.core.async :as async]
            ;[clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.pprint :as pprint]
            [clojure.stacktrace :as strace]
            [clj-json.core :as json]
            ;[pjson.core :as json]
            [taoensso.timbre :as logging]
            [taoensso.timbre.appenders.core :as appenders])
  (:gen-class))

;;;                           ____                     _           
;;;                          |  _ \  ___  ___ ___   __| | ___ _ __ 
;;;                          | | | |/ _ \/ __/ _ \ / _` |/ _ \ '__|
;;;                          | |_| |  __/ (_| (_) | (_| |  __/ |   
;;;                          |____/ \___|\___\___/ \__,_|\___|_|   
;;;
;;; Sample decoding application.  Multipart sentences that are out of order are dropped.
;;; (TODO: send to output channel for saving - can be reconciled later)
;;;


(def buffer-size 100)

;;---
;; Writers
;; ---

(defmulti write-data
  (fn [output-format & _] output-format))


; clj-json impl
(defmethod write-data "json" [_ writer data ]
  (.write writer (json/generate-string data)))


; pjson impl
(comment
(defmethod write-data "json" [_ writer data ]
  (.write writer (json/write-str data)))
)

(defmethod write-data "csv" [_ writer data]
  (csv/write-csv writer data))

(defmethod write-data :default [_ writer data]
  (write-data "csv" writer data))

(defn- write [filename output-format lines]
  (with-open [writer (clojure.java.io/writer filename)]
    (write-data output-format writer lines)))

;;---
;; Core
;;---

(defn- parse-types [types]
  (into #{} (map read-string (clojure.string/split types #","))))

(defn- extract-type [payload]
  (ais-vocab/char-str->dec (subs payload 0 1)))

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

(defn- process? [include-types msg]
  (contains? include-types (extract-type (msg :pl))))

(defn- log-metrics [dropped invalid error]
  (logging/info (format "count.dropped.total=%d" dropped))
  (logging/info (format "count.invalid.total=%d" invalid))
  (logging/info (format "count.error.total=%d" error)))

(defn- filter-stream [include-types in-ch]
  (let [out-ch (async/chan buffer-size)
	proc? (partial process? include-types)]
    (async/thread
      (loop [dropped 0
             invalid 0
	     error   0]
	(if-let [line (async/<!! in-ch)]
          (if-let [msg (ais-ex/tokenize line)]
            (if (= (msg :fn) 1)
	      ;; Only process
              (if (proc? msg)
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
                        (recur dropped invalid error))
                      ;; *** Multipart sentence, consume remaining fragments ***
                      (let [msgs (<<multipart msg in-ch)]
                        (async/>!! out-ch msgs)
                        (recur dropped invalid error)))
                    ;; Checksum failed verification
                    (do
                      (logging/error
                        (format "Invalid message checksum %s. [expected]%s != [actual]%s" (msg :en) chksum (msg :ck)))
                      (if (= frag-count 1)
                        (recur dropped (inc invalid) error)
			(do
			  (<<multipart msg in-ch)
                          (recur (+ dropped frag-count) invalid error))))))
                (let [^long frag-count (msg :fc)]
                  ;; *** Skip messages ***
                  (if (= (msg :fc) 1)
                    (recur (inc dropped) invalid error)
                    (do
                      ;(println (format "Skipping %s sentences" (msg :fc)))
		      (<<multipart msg in-ch)
                      (recur (+ dropped frag-count) invalid error)))))
	      (do
                (logging/warn (format "Multipart fragment received out or order - %s" line))))
                ;(recur (dropped) inc line)
            (do
              ;(logging/error (format "Error parsing message: %s" line))
              (recur dropped (inc invalid) (inc error))))
          (do
            (log-metrics dropped invalid error)
            (async/close! out-ch)))))
    out-ch))

(defn- process [format n in-ch]
  (let [out-ch (async/chan buffer-size)
        active-threads (atom n)]
    (dotimes [i n]
      (async/thread
        (loop [acc (transient [])]
          (if-let [msgs (async/<!! in-ch)]
	    (if-let [decoded (ais-core/parse format msgs)]
              (recur (conj! acc decoded))
              (recur acc))
            (do
              (logging/info (str "count.decoder.thread_" i "=" (count acc)))
              (async/>!! out-ch (persistent! acc)))))
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
        (time (do
        (logging/info (format "writing %s" (filename prefix i output-format)))
        (write (filename prefix i output-format) output-format batch)
        (logging/info (format "count.writer.thread_%s=%s" i (count batch)))
        (swap! active-threads dec)
        (when (= @active-threads 0)
          (async/>!! out-ch :done)
          (async/close! out-ch))))))
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
