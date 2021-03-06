(ns ais.mapping.type_10
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-10 (list
  {:len        6 :desc "Message Type"     :tag "type"      :fn (partial common/const 10)}
  {:len        2 :desc "Repeat Indicator" :tag "repeat"    :fn ais-types/u}
  {:len       30 :desc "Source MMSI"      :tag "mmsi"      :fn ais-types/u}
  {:len        2 :desc "Spare"            :tag "spare"     :fn ais-types/x}
  {:len       30 :desc "Destination MMSI" :tag "dest_mmsi" :fn ais-types/u}
  {:len        2 :desc "Spare"            :tag "spare"     :fn ais-types/x}
))
