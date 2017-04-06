(ns ais.mapping.type_8.d200f24
  (:require [ais.types :as ais-types]
            [ais.vocab :as ais-vocab]
            [ais.util :as ais-util]
            [ais.mapping.common :as common])
  (:gen-class))


(def gauge-fields (list
  {:len 11 :desc "Gauge ID"    :tag "id"    :fn ais-types/u}
  {:len 14 :desc "Water Level" :tag "level" :fn ais-types/i}
))

(defn field-mapper [_]
  gauge-fields)

(def bits-len (partial ais-types/array-bit-len 4 25))

(def seq-handler (partial ais-types/a 25 field-mapper ais-util/parse-binary))

(def d200f24 (list
  {:len       12 :desc "UN Country Code" :tag "country" :fn (partial ais-types/t ais-vocab/sixbit-ascii 2)}
  {:len bits-len :desc "Gauges"          :tag "gauges"  :fn seq-handler :a true}
))
