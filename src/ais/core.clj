(ns ais.core
  (:require [clojure.data.json :as json]
            [ais.extractors :as ais-ex]
            [ais.mapping.core :as ais-map]
            [ais.types :as ais-types]
            [ais.util :as ais-util]
            [ais.exceptions :refer :all])
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


(def tags ["c" "s" "n"])

(defn parse [data-format tag-block payload fill-bits]
  "Decodes complete ais message"
  (let [[acc collector] (data-collector data-format)
        bits (ais-util/pad (ais-util/payload->binary payload) fill-bits)]
    (ais-map/parse-binary (ais-map/parsing-rules bits)
                          (ais-map/parse-tag-block acc collector tags (if (nil? tag-block) "" tag-block))
                          collector bits)))

(defn parse-ais [data-format msg & frags]
  "Decodes a sequence of ais messages in a data-format data structure"
  (let [all (conj frags msg)]
    (parse data-format
           (ais-ex/parse "tags" msg)
           (reduce str (map (partial ais-ex/parse "payload") all))
           (ais-ex/parse "fill-bits" (last all)))))

(defn verify [& in-msgs]
  "Checks if sequence of messages have valid checksums and proper formatting.  Raises
  exceptions otherwise"
  (loop [msgs in-msgs
         verified []]
    (if-let [msg (first msgs)]
      (let [[env chksum] (ais-ex/parse "env-chksum" msg)]
        (if (not-any? nil? [env chksum])
          (if (= (ais-util/checksum env) chksum)
            (recur (rest msgs) (conj verified msg))
             (throw (ais.exceptions.ChecksumVerificationException.
	              (format "CHKSUM(%s) != %s, == %s" env chksum (ais-util/checksum env)))))
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
