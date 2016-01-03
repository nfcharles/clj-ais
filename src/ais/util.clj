(ns ais.util
  (:require [clojure.string :as string])
  (:gen-class))

(defn timestamp->iso 
  ([tstamp format]
    (->> (java.util.Date. tstamp)
         (.format (java.text.SimpleDateFormat. format))))
  ([tstamp]
    (timestamp->iso tstamp "yyyyMMdd'T'HHmmss'Z'")))


(defn checksum [msg]
  (let [sum (Integer/toString (reduce bit-xor 0 (map int (seq msg))) 16)]
    (if (== (count sum) 1)
      (string/upper-case (str "0" sum))
      (string/upper-case sum))))

(defn pad [payload num-fill-bits]
  (str payload (apply str (repeat num-fill-bits "0"))))

(defn bitmask [len]
  (Integer/parseInt (apply str (repeat len "1")) 2))

(defn twos-comp [bit-str]
  (->> (bit-not (Integer/parseInt bit-str 2))
       (bit-and (bitmask (count bit-str)))
       (+ 1)
       (* -1)))

(defn char-str->decimal [c]
  (let [tmp (- (int (.charAt c 0)) 48)]
    (if (> tmp 40) (- tmp 8) tmp)))

(defn char->decimal [c]
 (let [tmp (- (int c) 48)]
    (if (> tmp 40) (- tmp 8) tmp)))


(defn decimal->binary [num]
  (let [binary (Integer/toString num 2)
        len (count binary)]
    (str (->> (repeat (- 6 len) "0")
              (apply str))
         binary)))

(defn decimal->binary
  ([num str-len]
    (let [binary (Integer/toString num 2)
          len (count binary)]
      (str (->> (repeat (- str-len len) "0")
              (apply str)) binary)))
  ([num]
    (decimal->binary num 6)))