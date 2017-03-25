(ns ais.mapping.type_4
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def mapping-4 (list
  {:len  6 :desc "Message Type"           :tag "type"     :fn (partial common/const 4)}
  {:len  2 :desc "Repeat Indicator"       :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                   :tag "mmsi"     :fn ais-types/u}
  {:len 14 :desc "Year (UTC)"             :tag "year"     :fn ais-types/u}
  {:len  4 :desc "Month (UTC)"            :tag "month"    :fn ais-types/u}
  {:len  5 :desc "Day (UTC)"              :tag "day"      :fn ais-types/u}
  {:len  5 :desc "Hour (UTC)"             :tag "hour"     :fn ais-types/u}
  {:len  6 :desc "Minute (UTC)"           :tag "minute"   :fn ais-types/u}
  {:len  6 :desc "Second (UTC)"           :tag "second"   :fn ais-types/u}
  {:len  1 :desc "Fix quality"            :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longtitude"             :tag "lon"      :fn common/lon}
  {:len 27 :desc "Latitude"               :tag "lat"      :fn common/lat}
  {:len  4 :desc "Type of EPFD"           :tag "epfd"     :fn (partial ais-types/e ais-vocab/position-fix-type)}  
  {:len 10 :desc "Spare"                  :tag "spare"    :fn ais-types/x}
  {:len  1 :desc "RAIM flag"              :tag "raim"     :fn ais-types/b}
  {:len 19 :desc "SOTDMA state"           :tag "radio"    :fn ais-types/u}
))
