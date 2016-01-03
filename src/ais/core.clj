(ns ais.core
  (:require [clojure.data.json :as json])
  (:require [ais.extractors  :as ais-extractors])
  (:require [ais.mappings  :as ais-mappings])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
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
;; Core
;; ---
(defn valid-envelope? [envelope cksum]
  (= (ais-util/checksum envelope) cksum))

(defn payload->binary [msg]
  (->> (seq msg)
       (map #(ais-util/char->decimal %))
       (map #(ais-util/decimal->binary %))
       (apply str)))

(defn decode-binary-payload
  ([bits specs]
    (decode-binary-payload bits specs []))
  ([bits specs payload]
    (if-let [spec (first specs)]
      (let [block-len (spec :len)]
        (recur (subs bits block-len) 
               (rest specs) 
               (conj payload {(spec :tag) ((spec :fn) (subs bits 0 block-len))})))
      (apply merge payload))))

(defn split-type-binary-payload [bits]
  [(subs bits 0 6) (subs bits 6)])

(defn parse-binary [type binary-payload]
  (let [specs (ais-mappings/type-mapping (ais-types/u type))]
    (decode-binary-payload binary-payload specs)))

(defn parse-envelope [envelope]
  (let [bits (payload->binary (ais-extractors/extract-payload envelope))
       [type binary-payload] (split-type-binary-payload bits)]
    (parse-binary type binary-payload)))
    ;(if (not-any? nil? envelope-cksum)

(defn parse [line]
  (let [[envelope checksum] (ais-extractors/extract-envelope-checksum line)]
    (if (not-any? nil? [envelope checksum])
      (if (valid-envelope? envelope checksum)
        (try
          (parse-envelope envelope)
          (catch Exception e
	    {"error" (str "Exception: " e)}))
        {"error" (str "Checksum verification failed: " envelope checksum)})
      {"error" (str "Parse Error: Failed to extract envelope/checksum from message: " line)})))

;; ---
;; Multipart Core
;; ---

(defn parse-group [msgs]
  (let [[msg-1 msg-2] (sort-by ais-extractors/extract-fragment-number msgs)
        fill (ais-extractors/extract-fill-bits msg-2)
        payload (reduce str (map ais-extractors/extract-payload [msg-1 msg-2]))
        full-msg (str (ais-extractors/extract-packet-type msg-1) ",1,1,1,A," payload "," fill)
       ]
    (str full-msg "*" (ais-util/checksum full-msg))))

;;---
;; File I/O
;;---

(defn parse-from-file [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (doseq [line (line-seq rdr)]
      (println line))))

;;---
;; Entrypoint
;;---

; input (Integer. (nth args 0))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [envelope (nth args 0)]
   (println (json/write-str (parse envelope)))))
