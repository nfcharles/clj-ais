(ns ais.core
  (:require [clojure.data.json :as json])
  (:require [ais.extractors  :as ais-ex])
  (:require [ais.mappings  :as ais-mappings])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [clojure.stacktrace :as strace])
  (:gen-class))

;       _  _             _     
;   ___| |(_)       __ _(_)___ 
;  / __| || |_____ / _` | / __|
; | (__| || |_____| (_| | \__ \
;  \___|_|/ |      \__,_|_|___/
              


; message:   entire transmission
; mdata:     tag blocks
; envelope:  ais AVIDM, AVIDO sentence
; payload:   encoded data payload
;
; |------------------------------------------|
; | mdata |      |         |       |         |
; |------------------------------------------|
; ^       ^      ^         ^       ^         ^
; |       |      |         |       |         |
; |       |      pay_s     pay_e   |         |
; |       env_s                    env_e     |
; msg_s                                      msg_e


;; ---
;; Util
;; ---

(defmulti data-collector 
  (fn [data-format] data-format))

(defmethod data-collector "json" [_]
  [{} #(assoc %1 %2 %3)])

(defmethod data-collector "csv" [_]
  [[] #(conj %1 %3)])

(defmethod data-collector :default [_]
  (data-collector "csv"))

;; ---
;; Core
;; ---

(defn payload->binary [msg]
  (->> (seq msg)
       (map #(ais-util/char->decimal %))
       (map #(ais-util/decimal->binary %))
       (apply str)))

(defn decode-binary-payload [specs acc collector bits]
  (loop [left (count bits)
         b bits
         s specs
         a acc]
    (if-let [spec (first s)]
      (let [block-len (min (spec :len) left)]
        (recur (- left block-len)
               (subs b block-len) 
               (rest s)
               (collector a (spec :tag) ((spec :fn) (subs b 0 block-len)))))
      a)))

(defn parse-tag-block [acc collector line tags]
  (loop [t tags
         a acc]
    (if-let [tag (first t)]
      (let [tag-map (ais-mappings/tag-block tag)]
        (if-let [value ((tag-map :ex) line)]
          (recur (rest t)
                 (collector a (tag-map :tag) ((tag-map :fn) value))) ; non-null value, parse
          (recur (rest t)
	         (collector a (tag-map :tag) nil))))                 ; null value, pass thru
      a)))
    
(defn parse [data-format line]
  (let [[acc collector] (data-collector data-format)
        [envelope checksum] (ais-ex/extract-envelope-checksum line)]
    (if (not-any? nil? [envelope checksum])
      (if (= (ais-util/checksum envelope) checksum)
        (try
	  (let [bits (ais-util/pad
                       (payload->binary (ais-ex/extract-payload envelope)) 
                       (ais-ex/extract-fill-bits envelope))
                msg-type (ais-types/u (subs bits 0 6))
                metadata (parse-tag-block (collector acc "type" msg-type) collector line ["c" "s" "n"])]
            (decode-binary-payload (ais-mappings/select-map msg-type (subs bits 6)) ; type specification
                                   metadata                                         ; use metadata as initial accumulator
                                   collector                                        ; accumulator function
                                   (subs bits 6)))                                  ; raw binary payload
          (catch Exception e
            (strace/print-stack-trace e)
	    {"error" (str "Exception: " e)}))
        {"error" (str "Checksum verification failed: " envelope checksum)})
      {"error" (str "Parse Error: Failed to extract envelope/checksum from message: " line)})))


;; ---
;; Multipart Core
;; ---

(defn coalesce-group [msgs]
  ;; Coalesce multipart message into complete message
  (let [sorted (sort-by ais-ex/extract-fragment-number msgs)
        first-msg (first sorted)
        envelope (str
                   (ais-ex/extract-packet-type first-msg)      
                   ",1,1,1,A,"
                   (reduce str (map ais-ex/extract-payload sorted))
                   ","
                   (ais-ex/extract-fill-bits (last sorted)))]
    (str "\\" (ais-ex/extract-tag-block first-msg) "\\!" envelope "*" (ais-util/checksum envelope))))

;;---
;; Entrypoint
;;---

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [data-format (nth args 0)
        envelope (nth args 1)]
   (println (json/write-str (parse data-format envelope)))))
