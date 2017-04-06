(ns ais.mapping.common
  (:require [ais.types :as ais-types])
  (:gen-class))

(defn const [c & _] c)

(def lon  (partial ais-types/I 0.000001667 4))
(def lat  (partial ais-types/I 0.000001667 4))

(def mlon (partial ais-types/I 0.000016667 3))
(def mlat (partial ais-types/I 0.000016667 3))

(def llon (partial ais-types/I 0.000166667 2))
(def llat (partial ais-types/I 0.000166667 2))

(def xlon (partial ais-types/I 0.001666667 1))
(def xlat (partial ais-types/I 0.001666667 1))

(def speed  (partial ais-types/U 0.1 1))
(def course (partial ais-types/U 0.1 1))
