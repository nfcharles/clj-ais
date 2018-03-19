(ns ais.decode
  (:require [clojure.core.async :as async]
            [ais.logging :as ais-log]
            [taoensso.timbre :as logging]
            [ais.runner :as ais-runner])
  (:gen-class))


(def stdin-reader
  (java.io.BufferedReader. *in*))

(defn input-stream [reader]
  (async/to-chan (line-seq reader)))

(defn parse-output-prefix [args]
  (nth args 0))

(defn parse-decode-types [args]
  (ais-runner/parse-types (nth args 1)))

(defn parse-thread-count [args]
  (read-string (nth args 2)))

(defn parse-output-format [args]
  (nth args 3))

(defn parse-write-buffer-length [args]
  (read-string (nth args 4)))

(defn -main
  [& args]
  (try
    (ais-log/configure)
    (time (ais-runner/run (input-stream stdin-reader)
                          (parse-output-prefix args)
                          (parse-decode-types args)
                          (parse-thread-count args)
                          (parse-output-format args)
                          (parse-write-buffer-length args)))
    (catch Exception e
      (logging/fatal e))))
