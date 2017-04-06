(ns ais.types
  (:require [ais.util :as ais-util])
  (:require [clojure.string :as string])
  (:gen-class))

(defn u [rcrd bits]
  "Parses bits into unsigned integer."
  (Integer/parseInt bits 2))

(defn U
  "Parses bits into scaled unsigned integer; enders as float with
  d-places decimal places."
  ([scale d-places rcrd bits]
    (* (u rcrd bits) scale))
  ([scale d-places xformer rcrd bits]
    (xformer (U scale d-places rcrd bits))))

(def U1 (partial U 1.0 1))
(def U2 (partial U 1.0 2))
(def U3 (partial U 1.0 3))
(def U4 (partial U 1.0 4))

(defn i [rcrd bits]
  "Parses bits into a signed integer."
  (if (= "0" (subs bits 0 1))
    (Integer/parseInt bits 2)
    (ais-util/twos-comp bits)))

(defn I
  "Parses bits into scaled signed integer; renders as float with
  d-places decimal places."
  ([scale d-places rcrd bits]
    (* (i rcrd bits) scale))
  ([scale d-places xformer rcrd bits]
    (xformer (I scale d-places rcrd bits))))

(def I1 (partial I 1.0 1))
(def I2 (partial I 1.0 2))
(def I3 (partial I 1.0 3))
(def I4 (partial I 1.0 4))

(defn b [rcrd bits]
  "Parses bits into boolean."
  (if (== (u rcrd bits) 0) false true))

(defn e [vocab rcrd bits]
  "Parses bits into enumerated type (controlled vocabulary)."
  (vocab (u rcrd bits)))

(defn t [vocab len rcrd bits]
  "Parses bits into string (packed six-bit ASCII)."
  (loop [i 0
         left (count bits)
         b bits
         w []]
    (if (and (> len i) (>= left 6))
      (recur (inc i)
             (- left 6)
             (subs b 6)
             (conj w (e vocab rcrd (subs b 0 6))))
      (string/replace (apply str w) #"\s*(?:[@]*)?\s*$" ""))))
        
(defn x [rcrd bits]
  "Parses bits into spare of reserve bits (noop)."
  bits)

(defn d [rcrd bits]
  "Data bits - uninterpreted binary (noop)."
  bits)

(defn array-bit-len [max-size len rcrd bits]
  "Returns length of array bitfield.  The count of elements in the
  array are determined dynamically."
  (let [max-bits (* max-size len)
        n-bits (count bits)
	last-rcrd-len (mod (min max-bits n-bits) len)]
    (if (> last-rcrd-len 0)
      (throw (Exception. (format "Last record incomplete: %s bits missing." (- len last-rcrd-len))))
      (min max-bits (* (Math/floor (/ n-bits len)) len)))))

(defn static-array-bit-len [max-size len tag rcrd bits]
  "Returns length of array bitfield.  The count of elements in the array
  are determined from the specified field in the input rcrd."
  (let [bits-len (* (rcrd tag) len)
        max-bits (* max-size len)]
    (if (> bits-len max-bits)
      (throw (java.lang.Exception.
        (format "Calculated array bit length is too large: calc[%d], max-bits[%d]" bits-len max-bits)))
      bits-len)))

(defn a [len mapper parser collector rcrd bits]
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
               (conj acc (parser (mapper bit-fld) (transient {}) collector bit-fld))))
      acc)))
