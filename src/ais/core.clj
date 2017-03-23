(ns ais.core
  (:require [clojure.data.json :as json]
            [ais.extractors :as ais-ex]
            [ais.mapping.core :as ais-map]
            [ais.types :as ais-types]
            [ais.util :as ais-util])
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



(defmulti data-collector 
  (fn [data-format] data-format))

(defmethod data-collector "json" [_]
  [(transient {}) #(assoc! %1 %2 %3)])

(defmethod data-collector "csv" [_]
  [(transient []) #(conj! %1 %3)])

(defmethod data-collector :default [_]
  (data-collector "csv"))

(def tags ["c" "s" "n"])

(defn -parse [data-format tag-block payload fill-bits]
  "Decodes complete ais message"
  (let [[acc collector] (data-collector data-format)
        bits (ais-util/pad (ais-util/payload->binary payload) fill-bits)]
    (ais-map/parse-binary (ais-map/parsing-rules bits)
                          (ais-map/parse-tag-block acc collector tags (if (nil? tag-block) "" tag-block))
                          collector bits)))

(defn parse [data-format frags]
  "Organize sentence fragments for parsing"
  (-parse data-format
          ((first frags) :tg)
          (reduce str (map #(%1 :pl) frags))
          ((last frags) :fl)))

;;---
;; Entrypoint
;;---

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [data-format (nth args 0)
        message (nth args 1)]
   (println (json/write-str (parse data-format message)))))
