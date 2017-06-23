(ns ais.decode
  (:require [ais.util :as ais-util]
            [ais.extractors :as ais-ex]
            [ais.vocab :as ais-vocab]
            [ais.mapping.core :as ais-map]
            [ais.core :as ais-core]
            [ais.runner :as ais-runner]
            [clojure.core.async :as async]
            [clojure.data.csv :as csv]
            [clojure.pprint :as pprint]
            [clojure.stacktrace :as strace]
            [clj-json.core :as json]
            [taoensso.timbre :as logging]
            [taoensso.timbre.appenders.core :as appenders])
  (:gen-class))

(def stdin-reader
  (java.io.BufferedReader. *in*))

(defn -main
  [& args]
  (try
    (let [out-prefix (nth args 0)
          include-types (ais-runner/parse-types (nth args 1))
          n-threads (read-string (nth args 2))
          out-format (nth args 3)
          buffer-len (read-string (nth args 4))
          stream (async/to-chan (line-seq stdin-reader))]
      (ais-runner/configure-logging :std-err)
      (time (ais-runner/run stream
                            out-prefix
                            include-types
                            n-threads
                            out-format
                            buffer-len)))
    (catch Exception e
      (logging/fatal e))))
