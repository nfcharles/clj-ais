(ns ais.mapping.type_23
  (:require [ais.types :as ais-types]
            [ais.vocab :as ais-vocab]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-23 (list
  {:len   6 :desc "Message Type"     :tag "type"         :fn (partial common/const 23)}
  {:len   2 :desc "Repeat Indicator" :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"             :tag "mmsi"         :fn ais-types/u}
  {:len   2 :desc "Spare"            :tag "spare"        :fn ais-types/x}
  {:len  18 :desc "NE Longitude"     :tag "ne_lon"       :fn common/xlon}
  {:len  17 :desc "NE Latitude"      :tag "ne_lat"       :fn common/xlat}
  {:len  18 :desc "SW Longitude"     :tag "sw_lon"       :fn common/xlon}
  {:len  17 :desc "SW Latitude"      :tag "sw_lat"       :fn common/xlat}
  {:len   4 :desc "Station Type"     :tag "station_type" :fn (partial ais-types/e ais-vocab/station-type)}
  {:len   8 :desc "Ship Type"        :tag "ship_type"    :fn (partial ais-types/e ais-vocab/ship-type)}
  {:len  22 :desc "Spare"            :tag "spare"        :fn ais-types/x}
  {:len   2 :desc "Tx/Rx Mode"       :tag "txrx"         :fn ais-types/u}
  {:len   4 :desc "Report Interval"  :tag "interval"     :fn (partial ais-types/e ais-vocab/transmit-mode)}
  {:len   4 :desc "Quiet Time"       :tag "quiet"        :fn ais-types/u}
  {:len   6 :desc "Spare"            :tag "spare"        :fn ais-types/x}
))
