(ns ais.extractors
  (:gen-class))

;;
;; Matchers
;;

(def tag-block-matcher #"^\\(.+)\\")

(def group-matcher #"g:\d-(\d-\d\d\d\d).+AIVD[MO],\d,\d,\d?,([AB]?)")

(def timestamp-matcher #"^\\.*c:(\d*).*\\")

(def source-matcher #"^\\.*s:(\w*).*\\")

(def line-matcher #"^\\.*n:(\d*).*\\")

(def packet-type-matcher #"(AIVD[MO])")

(def channel-matcher #"AIVD[MO],\d,\d,\d?,([AB]?)")

(def envelope-matcher #"(AIVD[MO],\d,\d,\d?,[AB]?,.+,\d\*[A-F0-9][A-F0-9])")

(def fragment-count-matcher #"AIVD[MO],(\d)")

(def fragment-number-matcher #"AIVD[MO],\d,(\d)")

(def fragment-info-matcher #"AIVD[MO],(\d),(\d),")

(def payload-matcher #"AIVD[MO],\d,\d,\d?,[AB]?,(.+),")

(def fill-bits-matcher #"AIVD[MO],\d,\d,\d?,[AB]?,.+,(\d)")

(def checksum-matcher #"AIVD[MO],\d,\d,\d?,[AB]?,.+,\d\*([A-F0-9][A-F0-9])")

(def envelope-checksum-matcher #"(AIVD[MO],\d,\d,\d?,[AB]?,.+,\d)\*([A-Z0-9][A-Z0-9])")

;;
;; Extractors
;;

(defn extract-tag-block [message]
  (nth (re-find tag-block-matcher message) 1))

(defn extract-packet-type [message]
  (nth (re-find packet-type-matcher message) 1))

(defn extract-group [message]
  (if-let [group (re-find group-matcher message)]
    (apply str (rest group))))

; TODO: parse int here
(defn extract-timestamp [message]
  (nth (re-find timestamp-matcher message) 1))

(defn extract-source [message]
  (nth (re-find source-matcher message) 1))

(defn extract-line [message]
  (if-let [line-num (nth (re-find line-matcher message) 1)]
    (read-string line-num)
    nil))

(defn extract-envelope [message]
  (nth (re-find envelope-matcher message) 1))

(defn extract-fragment-count [message]
  (read-string (nth (re-find fragment-count-matcher message) 1)))

(defn extract-fragment-number [message]
  (read-string (nth (re-find fragment-number-matcher message) 1)))

(defn extract-fragment-info [message]
  (map read-string (rest (re-find fragment-info-matcher message))))

(defn extract-payload [message]
  (nth (re-find payload-matcher message) 1))

(defn extract-fill-bits [message]
  (read-string (nth (re-find fill-bits-matcher message) 1)))

(defn extract-checksum [message]
  (nth (re-find checksum-matcher message) 1))

(defn extract-envelope-checksum [message]
  (rest (re-find envelope-checksum-matcher message)))

(defn verified-message-syntax? [message]
  (== (count (extract-envelope-checksum message)) 2))
