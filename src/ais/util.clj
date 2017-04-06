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

(defn twos-comp [bits]
  (->> (bit-not (Integer/parseInt bits 2))
       (bit-and (ais-vocab/bitmask (count bits)))
       (+ 1)
       (* -1)))

(defn payload->binary [payload]
  "Convert ais sentence payload to binary string"
  (loop [pseq (seq payload)
         acc (transient [])]
    (if-let [c (first pseq)]
      (recur (rest pseq) (conj! acc (ais-vocab/char->bits c)))
      (apply str (persistent! acc)))))

(defn parse-binary [fields acc collector bits]
  "Takes a sequence of bits and constructs an output data structure via
  the collector function.  Bit sub strings are decoded via bitfield type
  handlers and accummulated via the collector function.  The initial accumulator
  is a transient data structure."
  (loop [flds fields
         rcrd acc
         n-bits (count bits)
         bts bits]
    (if-let [fld (first flds)]
      (if (fld :a) ;; array type
        (let [^long len ((fld :len) rcrd bts)]
          (recur (rest flds)
                 (collector rcrd (fld :tag) ((fld :fn) collector rcrd (subs bts 0 len)))
                 (- n-bits len)
                 (subs bts len)))
        (let [^long len (min (fld :len) n-bits)]
          (recur (rest flds)
                 (collector rcrd (fld :tag) ((fld :fn) rcrd (subs bts 0 len)))
                 (- n-bits len)
                 (subs bts len))))
      (persistent! rcrd))))
