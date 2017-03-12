(ns ais.mapping.type_7
  (:require [clojure.string :as string])
  (:require [ais.vocab :as ais-vocab])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mapping.common :as ais-map-comm])
  (:gen-class))

(def mmsi-seq-fields (list
  {:len 30 :desc "MMSI"     :tag "mmsi" :fn ais-types/u}
  {:len  2 :desc "Sequence" :tag "seq"  :fn ais-types/u}
))

(defn field-mapper [_]
  mmsi-seq-fields)

(def bits-len (partial ais-types/array-bit-len 4 32))

(def seq-handler (partial ais-types/a 32 field-mapper ais-map-comm/parse-binary))

(def mapping-7 (list
  {:len        6 :desc "Message Type"     :tag "type"   :fn (partial ais-map-comm/const 7)}
  {:len        2 :desc "Repeat Indicator" :tag "repeat" :fn ais-types/u}
  {:len       30 :desc "Source MMSI"      :tag "mmsi"   :fn ais-types/u}
  {:len        2 :desc "Spare"            :tag "spare"  :fn ais-types/x}
  {:len bits-len :desc "MMSI's"           :tag "mmsis"  :fn seq-handler :a true}
))
