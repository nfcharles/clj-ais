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
  (if (== (u bits) 0) "False" "True"))

(defn e [vocab bits]
  (vocab (u bits)))

(defn t 
  ([vocab len bits]
    (t vocab len bits 1 []))
  ([vocab len bits index ret]
    (if (> len index)
    (do
      (recur vocab 
             len
             (subs bits 6)
             (inc index)
             (conj ret (e vocab (subs bits 0 6)))))
      (string/replace (apply str ret) #"\s*(?:[@]*)?\s*$" ""))))

(defn x [bits] bits)

(defn d [bits] bits)

(defn a [size bits] bits)
