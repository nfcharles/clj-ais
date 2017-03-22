(ns ais.util
  (:require [clojure.string :as string])
  (:require [clj-time.coerce :as c])
  (:require [clj-time.format :as f])
  (:require [ais.vocab :as ais-vocab])
  (:gen-class))

(def not-nil? (complement nil?))

(defn timestamp->iso 
  ([^long ts formatter]
    (f/unparse formatter (c/from-long ts)))
  ([^long ts]
    (timestamp->iso ts (f/formatter "yyyyMMddHHmmss"))))

(defn checksum [msg]
  (let [sum (Integer/toString (reduce bit-xor 0 (map int (seq msg))) 16)]
    (if (== (count sum) 1)
      (string/upper-case (str "0" sum))
      (string/upper-case sum))))

(defn pad [payload n-bits]
  (str payload (apply str (repeat n-bits "0"))))

(defn bitmask [len]
  (Integer/parseInt (apply str (repeat len "1")) 2))

(defn twos-comp [bit-str]
  (->> (bit-not (Integer/parseInt bit-str 2))
       (bit-and (bitmask (count bit-str)))
       (+ 1)
       (* -1)))

(defn char->decimal [c]
 (let [tmp (- (int c) 48)]
    (if (> tmp 40) (- tmp 8) tmp)))

(defn payload->binary [payload]
  "Convert ais sentence payload to binary string"
  (loop [pseq (seq payload)
         acc []]
    (if-let [c (first pseq)]
      (recur (rest pseq) (conj acc (ais-vocab/char->bits c)))
      (apply str acc))))
