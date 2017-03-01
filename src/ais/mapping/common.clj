(ns ais.mapping.common
  (:require [ais.types :as ais-types])
  (:gen-class))

(defn const [c & _] c)

(def lon (partial ais-types/I (/ 1.0 600000) 4))

(def lat (partial ais-types/I (/ 1.0 600000) 4))