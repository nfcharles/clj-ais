(ns ais.mapping.type_22
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-22 (list
  {:len   6 :desc "Message Type"     :tag "type"      :fn (partial common/const 22)}
  {:len   2 :desc "Repeat Indicator" :tag "repeat"    :fn ais-types/u}
  {:len  30 :desc "MMSI"             :tag "mmsi"      :fn ais-types/u}
  {:len   2 :desc "Spare"            :tag "spare"     :fn ais-types/x}
  {:len  12 :desc "Channel A"        :tag "channel_a" :fn ais-types/u}
  {:len  12 :desc "Channel B"        :tag "channel_b" :fn ais-types/u}
  {:len   4 :desc "Tx/Rx Mode"       :tag "txrx"      :fn ais-types/u}
  {:len   1 :desc "Power"            :tag "power"     :fn ais-types/b}
  {:len  18 :desc "NE Longitude"     :tag "ne_lon"    :fn common/xlon}
  {:len  17 :desc "NE Latitude"      :tag "ne_lat"    :fn common/xlat}
  {:len  18 :desc "SW Longitude"     :tag "sw_lon"    :fn common/xlon}
  {:len  17 :desc "SW Latitude"      :tag "sw_lat"    :fn common/xlat}
  {:len   1 :desc "Addressed"        :tag "addressed" :fn ais-types/b}
  {:len   1 :desc "Channel A Band"   :tag "band_a"    :fn ais-types/b}
  {:len   1 :desc "Channel B Band"   :tag "band_b"    :fn ais-types/b}
  {:len   3 :desc "Zone Size"        :tag "zonesize"  :fn ais-types/u}
  {:len  23 :desc "Spare"            :tag "spare"     :fn ais-types/x}
))
