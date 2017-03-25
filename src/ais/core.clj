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


(defn payload [n frags]
  (if (= n 1)
    ((nth frags 0) :pl)
    (loop [i 0
           acc []]
      (if (< i n)
        (recur (inc i) (conj acc ((nth frags i) :pl)))
        (apply str acc)))))

(defn parse [data-format frags]
  "Organize sentence fragments for parsing"
  (let [n (count frags)
        lead (nth frags 0)]
    (ais-map/parse data-format
                   (lead :ty)
                   (lead :tg)
		   (payload n frags)
                   ((nth frags (- n 1)) :fl))))

;;---
;; Entrypoint
;;---

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [data-format (nth args 0)
        message (nth args 1)]
   (println (json/write-str (parse data-format message)))))
