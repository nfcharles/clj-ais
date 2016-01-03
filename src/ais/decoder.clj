(ns ais.decoder
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mappings :as ais-map])
  (:require [ais.core :as ais-core])
  (:require [clojure.core.async :as async])
  (:require [clojure.data.json :as json])
  (:require [clojure.pprint :as pprint])
  (:gen-class))

;;;; Sample implemenation of decoding application

;; Operating assumption: Group values stream in sequential order

;; CONSTANTS
(def buffer-size 1000)

;;---
;; Util
;;--

;; TODO: micro-op: can perform less total bit shifts by rebinding mask and left shifting by 1
(defn- parse-runtime-types [types]
  (loop [i 0
         acc {}]
    (if (< i 27)
      (let [mask (bit-shift-left 1 i)]
        (recur (inc i) (assoc acc (inc i) (not= (bit-and types mask) 0))))
      acc)))

(defn- type-supported? [types msg-type]
  (types msg-type))

(defn extract-type [msg]
  (ais-util/char-str->decimal (subs (ais-ex/extract-payload msg) 0 1)))

(defn fragment-count [msg]
  (ais-ex/extract-fragment-count msg))

;;---
;; Core
;;---

(defn decode [msg]
  (try
    (ais-core/parse msg)
    (catch Exception e
      (.println *err* (str "Error decoding - " msg ". " e))))) 

(defn parse-group [& msgs]
  (try
    (ais-core/parse-group msgs)
    (catch Exception e
      (.println *err* (str "Error parsing multipart message - " msgs ". " e))
      "failed")))
      
(defn split-stream [in-ch decode-types]
  (let [out-ch (async/chan buffer-size)
        type-map (parse-runtime-types decode-types)
        valid-type (partial type-supported? type-map)]
    (async/thread
      (loop []
        (if-let [line (async/<!! in-ch)]
          (do 
            (when (ais-ex/verified-message-syntax? line)
              (let [msg-type   (extract-type line)
                    frag-count (ais-ex/extract-fragment-count line)
	            frag-num   (ais-ex/extract-fragment-number line)]
                (if (and (valid-type msg-type) (= frag-num 1))
	          (condp = frag-count
	            1 (async/>!! out-ch line)
	    	    2 (async/>!! out-ch (parse-group line (async/<!! in-ch)))
	     	    (println (str "Unexpected fragment count: " frag-count ". " line)))
		  (.println *err* (str "Dropping [type=" msg-type "] " line)))))
             (recur))
           (async/close! out-ch))))
    out-ch))

(defn process [n in-ch]
  (let [out-ch (async/chan buffer-size)]
    (dotimes [i n]
      (println (str "thread-" i))
      (async/thread
        (loop []
          (when-let [line (async/<!! in-ch)]
	    (async/>!! out-ch (decode line))
	    (recur)))
        (async/close! out-ch)))
    out-ch))

(defn- write [path prefix lines]
  (let [filename (str path "/" prefix ".json")]
    (println (str "writing " filename))
    (spit filename lines)))

(defn- collect [in-ch]
  (loop [msgs []]
    (if-let [line (async/<!! in-ch)]
      (recur (conj msgs line))
      msgs)))

(defn- collect [in-ch]
  (let [out-ch (async/chan)]
    (async/thread
      (loop [msgs []]
        (if-let [line (async/<!! in-ch)]
          (recur (conj msgs line))
          (async/>!! out-ch msgs))))
    out-ch))

(defn run [in-ch decode-types nthreads]
  (let [filtered (split-stream in-ch decode-types)
       	processed (process nthreads filtered)
        msgs (async/<!! (collect processed))]
    (println (str "writing " (count msgs) " messages."))
    (write "/tmp" "foo"  (json/write-str msgs))))

(def stdin-reader
  (java.io.BufferedReader. *in*))

(defn -main
  [& args]
  (let [decode-types (Integer. (nth args 0))
        nthreads (Integer. (nth args 1))
        stream (async/to-chan (line-seq stdin-reader))]
    (time (run stream decode-types nthreads))))
