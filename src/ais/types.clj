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

(defn a [size bits] bits)
