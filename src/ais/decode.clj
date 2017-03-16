(ns ais.decode
  (:require [ais.util :as ais-util]
            [ais.extractors :as ais-ex]
            [ais.mapping.core :as ais-map]
            [ais.core :as ais-core]
            [clojure.core.async :as async]
            [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.pprint :as pprint]
            [clojure.stacktrace :as strace]
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

(defn- consume [base n in-ch]
  (loop [acc [base]
         i n]
    (if (> i 0)
      (if-let [line (async/<!! in-ch)]
        (recur (conj acc line) (dec i))
	acc)
      acc)))

(defn- log-metrics [dropped invalid]
  (logging/info (format "count.invalid.total=%d" invalid))
  (logging/info (format "count.dropped.total=%d" dropped)))

;; TODO: if multipart types are not in include types, consume remainder fragments and then discard
;; this prevents fragments receieved out of order exception.
(defn- filter-stream [include-types in-ch]
  (let [out-ch (async/chan buffer-size)
        unpaired-frags (atom {})]
    (async/thread
      (loop [dropped 0
             invalid 0]
        (if-let [line (async/<!! in-ch)]
          (if (valid-syntax? line)
            (if (= (m_fn line) 1) ; start message (single | multipart)
              (if (contains? include-types (m_type line))
                (let [n-frags (m_fc line)]
                  (if (= n-frags 1)
                    (do
                      (async/>!! out-ch [line])
                      (recur dropped invalid))
                    (do
                      (let [lines (consume line (- n-frags 1) in-ch)]
                        (async/>!! out-ch lines)
                        (recur dropped invalid)))))
		(do ;; if multipart consume the remainder of elements
		  (let [^long n-frags (m_fc line)]
		    (if (> n-frags 1)
		      (do
                        ;; parse thru skipped sentences
			;(println (format "Skipping %s sentences" n-frags))
		        (consume line (- n-frags 1) in-ch)
		        (recur (+ dropped n-frags) invalid))
                      (recur (inc dropped) invalid)))))
	      (do
                (println (format "Multipart fragment received out or order - %s" line))))
		;(recur (dropped) inc line)
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
