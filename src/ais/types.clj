(ns ais.types
  (:require [ais.util :as ais-util])
  (:require [clojure.string :as string])
  (:gen-class))

(defn u [bits]
  "Parses bits into unsigned integer."
  (Integer/parseInt bits 2))

(defn U
  "Parses bits into scaled unsigned integer; enders as float with
  d-places decimal places."
  ([scale d-places bits]
    (* (u bits) scale))
  ([scale d-places xformer bits]
    (xformer (U scale d-places bits))))

(defn i [bits]
  "Parses bits into a signed integer."
  (if (= "0" (subs bits 0 1))
    (Integer/parseInt bits 2)
    (ais-util/twos-comp bits)))

(defn I
  "Parses bits into scaled signed integer; renders as float with
  d-places decimal places."
  ([scale d-places bits]
    (* (i bits) scale))
  ([scale d-places xformer bits]
    (xformer (I scale d-places bits))))

(defn b [bits]
  "Parses bits into boolean."
  (if (== (u bits) 0) false true))

(defn e [vocab bits]
  "Parses bits into enumerated type (controlled vocabulary)."
  (vocab (u bits)))

(defn t [vocab len bits]
  "Parses bits into string (packed six-bit ASCII)."
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
        

(defn x [bits]
  "Parses bits into spare of reserve bits (noop)."
  bits)

(defn d [bits]
  "Data bits - uninterpreted binary (noop)."
  bits)


(defn array-bit-len [max-size len rcrd bits]
  "Returns length of array bitfield.  The count of elements in the
  array are determined dynamically."
  (let [max-bits (* max-size len)]
    (min max-bits (* (Math/floor (/ (count bits) len)) len))))

(defn static-array-bit-len [max-size len tag rcrd bits]
  "Returns length of array bitfield.  The count of elements in the array
  are determined from the specified field in the input rcrd."
  (let [bits-len (* (rcrd tag) len)
        max-len (* max-size len)]
    (if (> bits-len max-len)
      (throw (java.lang.Exception.
        (format "Calculated array bit length is too large: calc[%d], max-len[%d]" bits-len max-len)))
      bits-len)))

(defn a [len mapper parser collector record bits]
  "Parses bits into array.  Mapper determines the collection of fields that make
  up an array entry.  Parser interprets bitfields and aggregates into decoded values
  via the collector function.  Returns a sequence of parsed (decoded) bits."
  (loop [i (Math/floor (/ (count bits) len))
         bts bits
         acc []]
    (if (> i 0)
      (let [bit-fld (subs bts 0 len)]
        (recur (dec i)
               (subs bts len)
               (conj acc (parser (mapper bit-fld) {} collector bit-fld))))
      acc)))
