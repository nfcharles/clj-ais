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

(defmulti output-type-handler 
  (fn [o-type] o-type))

(defmethod output-type-handler "json" [_]
  [{} #(assoc %1 %2 %3)])

(defmethod output-type-handler "csv" [_]
  [[] #(conj %1 %3)])

(defmethod output-type-handler :default [_]
  (output-type-handler "csv"))

;; ---
;; Core
;; ---

;; TODO: Sleep, wake up, clean up organization

(defn valid-envelope? [envelope cksum]
  (= (ais-util/checksum envelope) cksum))

(defn payload->binary [msg]
  (->> (seq msg)
       (map #(ais-util/char->decimal %))
       (map #(ais-util/decimal->binary %))
       (apply str)))

(defn decode-binary-payload [specs acc collector bits]
  (loop [b bits
         s specs
         a acc]
    (if-let [spec (first s)]
      (let [block-len (spec :len)]
        (recur (subs b block-len) 
               (rest s)
               (collector a (spec :tag) ((spec :fn) (subs b 0 block-len)))))
      a)))

(defn parse-envelope [acc collector envelope]
  (let [bits (ais-util/pad (payload->binary (ais-ex/extract-payload envelope)) (ais-ex/extract-fill-bits envelope))]
    (decode-binary-payload (ais-mappings/msg-spec (ais-types/u (subs bits 0 6)))
                            acc 
    			    collector
			    (subs bits 6))))

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
    
(defn parse [output-type line]
  (let [[acc collector] (output-type-handler output-type)
        [envelope checksum] (ais-ex/extract-envelope-checksum line)
         metadata (parse-tag-block acc collector line ["c" "s" "n"])] ; Let's not hardcode this
    (if (not-any? nil? [envelope checksum])
      (if (valid-envelope? envelope checksum)
        (try
          (parse-envelope metadata collector envelope)
          (catch Exception e
            (strace/print-stack-trace e)
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
  (let [output-type (nth args 0)
        envelope (nth args 1)]
   (println (json/write-str (parse output-type envelope)))))
