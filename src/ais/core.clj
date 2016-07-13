(ns ais.core
  (:require [clojure.data.json :as json])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mapping.core :as ais-mappings])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [ais.exceptions :refer :all])
  (:gen-class))


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

(defn- char->binary [c]
  (ais-util/decimal->binary 
   (ais-util/char->decimal c)))

(defn payload->binary [payload]
  (->> (seq payload)
       (map char->binary)
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
               (collector a 
                          (spec :tag) 
                          ((spec :fn) (subs b 0 block-len)))))
      a)))

(defn parse-tag-block [acc collector tags block]
  (loop [t tags
         a acc]
    (if-let [tag (first t)]
      (let [spec (ais-mappings/tag-mapping tag)
            value ((spec :ex-fn) block)]
        (recur (rest t)
               (collector a (spec :tag) (if (nil? value) nil ((spec :fn) value)))))
      a)))

(defn parse [data-format tag-block payload fill-bits]
  (let [[acc collector] (data-collector data-format)
        bits (ais-util/pad (payload->binary payload) fill-bits)]
    (decode-binary-payload (ais-mappings/parsing-rules bits)                    ; msg-type field decoding specs
                           (parse-tag-block acc 
                                            collector 
                                            ["c" "s" "n"] 
                                            (if (nil? tag-block) "" tag-block)) ; use tag metadata as initial accumulator
                           collector                                            ; accumulator function
                           bits)))                                              ; raw binary payload

(defn parse-ais [data-format msg & frags]
  (let [all (conj frags msg)]
    (parse data-format
           (ais-ex/parse "tags" msg)
           (reduce str (map (partial ais-ex/parse "payload") all))
           (ais-ex/parse "fill-bits" (last all)))))

(defn verify [& in-msgs]
  (loop [msgs in-msgs
         verified []]
    (if-let [msg (first msgs)]
      (let [[env chksum] (ais-ex/parse "env-chksum" msg)]
        (if (not-any? nil? [env chksum])
          (if (= (ais-util/checksum env) chksum)
            (recur (rest msgs) (conj verified msg))
             (throw (ais.exceptions.ChecksumVerificationException. (format "CHKSUM(%s) != %s, == %s" env chksum (ais-util/checksum env)))))
          (throw (ais.exceptions.MessageFormatException. (str "Failed parsing (env, chksum) from " msg)))))
      verified)))

;;---
;; Entrypoint
;;---

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [data-format (nth args 0)
        message (nth args 1)]
   (println (json/write-str (parse-ais data-format message)))))
