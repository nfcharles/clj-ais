(ns ais.core
  (:require [clojure.data.json :as json])
  (:require [ais.extractors  :as ais-ex])
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
  (let [fill (ais-ex/extract-fill-bits envelope)
        bits (ais-util/pad (payload->binary (ais-ex/extract-payload envelope)) fill)
       [type binary-payload] (split-type-binary-payload bits)]
    (parse-binary type binary-payload)))

;; TODO: Parse all available fields in tag block.
(defn parse-tag-block [line]
  (let [timestamp (ais-ex/extract-timestamp line)]
    (if (nil? timestamp) 
        (hash-map "timestamp" timestamp)
        (hash-map "timestamp" (ais-util/timestamp->iso (* 1000 (read-string timestamp)))))))

(defn parse [line]
  (let [metadata (parse-tag-block line)
       [envelope checksum] (ais-ex/extract-envelope-checksum line)]
    (if (not-any? nil? [envelope checksum])
      (if (valid-envelope? envelope checksum)
        (try
	  (merge metadata (parse-envelope envelope))
          (catch Exception e
	    {"error" (str "Exception: " e)}))
        {"error" (str "Checksum verification failed: " envelope checksum)})
      {"error" (str "Parse Error: Failed to extract envelope/checksum from message: " line)})))

;; ---
;; Multipart Core
;; ---

(defn parse-group [msgs]
  ;; Reassemble group message parts into complete message.
  (let [[msg-1 msg-2] (sort-by ais-ex/extract-fragment-number msgs)
        tag-block (ais-ex/extract-tag-block msg-1)
        payload (reduce str (map ais-ex/extract-payload [msg-1 msg-2]))
        fill (ais-ex/extract-fill-bits msg-2)
        msg (str (ais-ex/extract-packet-type msg-1) ",1,1,1,A," payload "," fill)
       ]
    (str "\\" tag-block "\\!" msg "*" (ais-util/checksum msg))))

;;---
;; Entrypoint
;;---

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [envelope (nth args 0)]
   (println (json/write-str (parse envelope)))))
