(ns ais.mapping.type_14
  (:require [ais.types :as ais-types])
  (:require [ais.vocab :as ais-vocab])
  (:require [ais.mapping.common :as ais-map-comm])
  (:gen-class))

(def mapping-14 (list
  {:len        6 :desc "Message Type"     :tag "type"       :fn (partial ais-map-comm/const 14)}
  {:len        2 :desc "Repeat Indicator" :tag "repeat"     :fn ais-types/u}
  {:len       30 :desc "Source MMSI"      :tag "mmsi"       :fn ais-types/u}
  {:len        2 :desc "Spare"            :tag "spare"      :fn ais-types/x}
  {:len      968 :desc "Text"             :tag "text"       :fn (partial ais-types/t ais-vocab/sixbit-ascii 26)}
))
