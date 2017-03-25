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
  (loop [mseq (seq msg)
         sum 0]
    (if-let [c (first mseq)]
      (recur (rest mseq) (bit-xor sum (int c)))
      (format "%02X" sum))))

(defn pad [payload n]
  (str payload (apply str (repeat n "0"))))

(defn bitmask [n]
  (Integer/parseInt (apply str (repeat n "1")) 2))

(defn twos-comp [bits]
  (->> (bit-not (Integer/parseInt bits 2))
       (bit-and (bitmask (count bits)))
       (+ 1)
       (* -1)))

(defn payload->binary [payload]
  "Convert ais sentence payload to binary string"
  (loop [pseq (seq payload)
         acc (transient [])]
    (if-let [c (first pseq)]
      (recur (rest pseq) (conj! acc (ais-vocab/char->bits c)))
      (apply str (persistent! acc)))))
