(ns ais.mapping.type_17
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-17 (list
  {:len   6 :desc "Message Type"     :tag "type"   :fn (partial common/const 17)}
  {:len   2 :desc "Repeat Indicator" :tag "repeat" :fn ais-types/u}
  {:len  30 :desc "Source MMSI"      :tag "mmsi"   :fn ais-types/u}
  {:len   2 :desc "Spare"            :tag "spare"  :fn ais-types/x}
  {:len  18 :desc "Longitude"        :tag "lon"    :fn common/xlon}
  {:len  17 :desc "Latitude"         :tag "lat"    :fn common/xlon}
  {:len   5 :desc "Spare"            :tag "spare"  :fn ais-types/x}
  {:len 736 :desc "Payload"          :tag "data"   :fn ais-types/d}
))
