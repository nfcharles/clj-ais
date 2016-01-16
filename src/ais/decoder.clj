(ns ais.decoder
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mappings :as ais-map])
  (:require [ais.core :as ais-core])
  (:require [clojure.core.async :as async])
  (:require [clojure.data.json :as json])
  (:require [clojure.data.csv :as csv])
  (:require [clojure.pprint :as pprint])
  (:gen-class))

;   ____                        _        ____                     _           
;  / ___|  __ _ _ __ ___  _ __ | | ___  |  _ \  ___  ___ ___   __| | ___ _ __ 
;  \___ \ / _` | '_ ` _ \| '_ \| |/ _ \ | | | |/ _ \/ __/ _ \ / _` |/ _ \ '__|
;   ___) | (_| | | | | | | |_) | |  __/ | |_| |  __/ (_| (_) | (_| |  __/ |   
;  |____/ \__,_|_| |_| |_| .__/|_|\___| |____/ \___|\___\___/ \__,_|\___|_|   
;                        |_|                                                  


(def buffer-size 1000)

(def msg-types (into {} (for [k (range 1 28)] [k false])))

;;---
;; Writers
;; ---

(defmulti write-data 
  (fn [output-type & _] 
    output-type))

(defmethod write-data "json" [_ writer data ]
  (.write writer (json/write-str data)))

(defmethod write-data "csv" [_ writer data]
  (csv/write-csv writer data))

(defmethod write-data :default [_ writer data]
  (write-data "csv" writer data))

(defn- write [name output-type lines]
  (let [filename (str  name "." output-type)]
    (println (str "writing " filename))
    (with-open [w (clojure.java.io/writer filename)]
      (write-data output-type w lines))))

;;---
;; Util
;;--

(defn- handled-types [types]
  (apply merge msg-types types))

(defn- parse-types [types]
  (into {} (for [k (clojure.string/split types #",")] [(read-string k) true])))

(defn- extract-type [msg]
  (ais-util/char-str->decimal (subs (ais-ex/extract-payload msg) 0 1)))

(defn- fragment-count [msg]
  (ais-ex/extract-fragment-count msg))

;;---
;; Core
;;---

(defn- decode [output-type msg]
  (try
    (ais-core/parse output-type msg)
    (catch Exception e
      (.println *err* (str "Error decoding - " msg ". " e))))) 

(defn- parse-group [& msgs]
  (try
    (ais-core/coalesce-group msgs)
    (catch Exception e
      (.println *err* (str "Error parsing multipart message - " msgs ". " e))
      "failed")))
      
(defn- split-stream [in-ch types]
  (let [out-ch (async/chan buffer-size)]
    (async/thread
      (loop []
        (if-let [line (async/<!! in-ch)]
          (do 
            (when (ais-ex/verified-message-syntax? line)
              (let [msg-type   (extract-type line)
                    frag-count (ais-ex/extract-fragment-count line)
	            frag-num   (ais-ex/extract-fragment-number line)]
                (if (and (types msg-type) (= frag-num 1))
	          (condp = frag-count
	            1 (async/>!! out-ch line)
		    ;; Assumption: Group values stream in sequential order
	    	    2 (async/>!! out-ch (parse-group line (async/<!! in-ch)))
	     	    (.println *err* (str "Unexpected fragment count: " frag-count ". " line)))
		  (.println *err* (str "Dropping [type=" msg-type "] " line)))))
             (recur))
           (async/close! out-ch))))
    out-ch))

(defn- process [output-type n in-ch]
  (let [out-ch (async/chan buffer-size)]
    (dotimes [i n]
      (println (str "thread-" i))
      (async/thread
        (loop []
          (when-let [line (async/<!! in-ch)]
	    (async/>!! out-ch (decode output-type line))
	    (recur)))
        (async/close! out-ch)))
    out-ch))

(defn- collect [in-ch]
  (let [out-ch (async/chan)]
    (async/thread
      (loop [msgs []]
        (if-let [line (async/<!! in-ch)]
          (recur (conj msgs line))
          (async/>!! out-ch msgs))))
    out-ch))

(defn run [in-ch output-filename types nthreads output-type]
  (let [filtered (split-stream in-ch types)
       	processed (process output-type nthreads filtered)
        msgs (async/<!! (collect processed))]
    (println (str "writing " (count msgs) " messages."))
    (write output-filename output-type msgs)))

(def stdin-reader
  (java.io.BufferedReader. *in*))

(defn -main
  [& args]
  (let [output-filename (nth args 0)
        types (handled-types (parse-types (nth args 1)))
        nthreads (Integer. (nth args 2))
	output-type (nth args 3)
        stream (async/to-chan (line-seq stdin-reader))]
    (time (run stream output-filename types nthreads output-type))))
