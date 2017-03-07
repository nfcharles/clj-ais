(ns ais.types
  (:require [ais.util :as ais-util])
  (:require [clojure.string :as string])
  (:gen-class))

(defn u [bits]
  (Integer/parseInt bits 2))

(defn U 
  ([scale d-places bits]
    (* (u bits) scale))
  ([scale d-places xformer bits]
    (xformer (U scale d-places bits))))

(defn i [bits]
  (if (= "0" (subs bits 0 1))
    (Integer/parseInt bits 2)
    (ais-util/twos-comp bits)))

(defn I 
  ([scale d-places bits]
    (* (i bits) scale))
  ([scale d-places xformer bits]
    (xformer (I scale d-places bits))))

(defn b [bits]
  (if (== (u bits) 0) false true))

(defn e [vocab bits]
  (vocab (u bits)))

(defn t [vocab len bits]
  (loop [i 0
         left (count bits)
         b bits
         w []]
    (if (and (> len i) (>= left 6))
      (recur (inc i)
             (- left 6)
             (subs b 6)
             (conj w (e vocab (subs b 0 6))))
      (string/replace (apply str w) #"\s*(?:[@]*)?\s*$" ""))))
        

(defn x [bits] bits)

(defn d [bits] bits)


(defn array-bit-len [max-size len acc bits]
  (let [max-bits (* max-size len)]
    (min max-bits (* (Math/floor (/ (count bits) len)) len))))

(defn static-array-bit-len [max-size len tag acc bits]
  (let [bits-len (* (acc tag) len)
        max-len (* max-size len)]
    (if (> bits-len max-len)
      (throw (java.lang.Exception.
        (format "Calculated array bit length is too large: calc[%d], max-len[%d]" bits-len max-len)))
      bits-len)))

(defn a [len mapper parser collector record bits]
  (loop [i (Math/floor (/ (count bits) len))
         bts bits
         acc []]
    (if (> i 0)
      (let [bit-fld (subs bts 0 len)]
        (recur (dec i)
               (subs bts len)
               (conj acc (parser (mapper bit-fld) {} collector bit-fld))))
      acc)))
