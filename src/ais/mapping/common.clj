(ns ais.mapping.common
  (:require [ais.types :as ais-types])
  (:gen-class))

(defn const [c & _] c)

(def lon (partial ais-types/I (/ 1.0 600000) 4))

(def lat (partial ais-types/I (/ 1.0 600000) 4))

(def slon (partial ais-types/I (/ 1.0 6000000) 3))

(def slat (partial ais-types/I (/ 1.0 6000000) 3))

(def speed (partial ais-types/U (/ 1.0 10) 1))

(defn parse-binary [fields acc collector bits]
  "Takes a sequence of bits and constructs an output data structure via
  the collector function.  Bitfields are processed via field type handlers
  and accummulated via the collector function.  The initial accumulator is
  a transient data structure."
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
