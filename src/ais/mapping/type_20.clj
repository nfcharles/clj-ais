(ns ais.mapping.type_20
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def offset-seq-fields (list
  {:len  12 :desc "Offset"         :tag "offset"     :fn ais-types/u}
  {:len   4 :desc "Reserved slots" :tag "number"     :fn ais-types/u}
  {:len   3 :desc "Time-out"       :tag "timeout"    :fn ais-types/u}
  {:len  11 :desc "Increment"      :tag "increment"  :fn ais-types/u}
))

(defn field-mapper [_]
  offset-seq-fields)

(def bits-len (partial ais-types/array-bit-len 4 30))

(def seq-handler (partial ais-types/a 30 field-mapper common/parse-binary))

(def mapping-20 (list
  {:len        6 :desc "Message Type"     :tag "type"    :fn (partial common/const 20)}
  {:len        2 :desc "Repeat Indicator" :tag "repeat"  :fn ais-types/u}
  {:len       30 :desc "MMSI"             :tag "mmsi"    :fn ais-types/u}
  {:len        2 :desc "Spare"            :tag "spare"   :fn ais-types/x}
  {:len bits-len :desc "Offsets"          :tag "offsets" :fn seq-handler :a true}
))

