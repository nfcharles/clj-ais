(ns ais.mapping.type_15
  (:require [ais.types :as ais-types]
            [ais.util :as ais-util]
            [ais.mapping.common :as common])
  (:gen-class))


(def type-seq-fields (list
  {:len  6 :desc "Message Type" :tag "type1"  :fn ais-types/u}
  {:len 12 :desc "Slot Offset"  :tag "offset" :fn ais-types/u}
  {:len  2 :desc "Spare"        :tag "spare"  :fn ais-types/x}
))

(defn field-mapper [_]
  type-seq-fields)

(def bits-len (partial ais-types/array-bit-len 2 20))

(def seq-handler (partial ais-types/a 20 field-mapper ais-util/parse-binary))

(def mapping-15 (list
  {:len        6 :desc "Message Type"      :tag "type"   :fn (partial common/const 15)}
  {:len        2 :desc "Repeat Indicator"  :tag "repeat" :fn ais-types/u}
  {:len       30 :desc "Source MMSI"       :tag "mmsi"   :fn ais-types/u}
  {:len        2 :desc "Spare"             :tag "spare"  :fn ais-types/x}
  {:len       30 :desc "Interrogated MMSI" :tag "mmsi1"  :fn ais-types/u}
  {:len bits-len :desc "Types"             :tag "types"  :fn seq-handler :a true}
  {:len       30 :desc "Interrogated MMSI" :tag "mmsi2"  :fn ais-types/u}
  {:len        6 :desc "Message Type"      :tag "type2"  :fn ais-types/u}
  {:len       12 :desc "Slot Offset"       :tag "offset" :fn ais-types/u}
  {:len        2 :desc "Spare"             :tag "spare"  :fn ais-types/x}
))
