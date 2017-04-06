(ns ais.mapping.type_9
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-9 (list
  {:len  6 :desc "Message Type"             :tag "type"     :fn (partial common/const 9)}
  {:len  2 :desc "Repeat Indicator"         :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                     :tag "mmsi"     :fn ais-types/u}
  {:len 12 :desc "Altitude"                 :tag "alt"      :fn ais-types/u}
  {:len 10 :desc "Speed Over Ground (SOG)"  :tag "speed"    :fn ais-types/u}
  {:len  1 :desc "Position Accuracy"        :tag "accuracy" :fn ais-types/u}
  {:len 28 :desc "Lontitude"                :tag "lon"      :fn common/lon}
  {:len 27 :desc "Latitude"                 :tag "lat"      :fn common/lat}
  {:len 12 :desc "Course Over Ground (COG)" :tag "course"   :fn common/course}
  {:len  6 :desc "Time Stamp"               :tag "second"   :fn ais-types/u}
  {:len  8 :desc "Regional Reservered"      :tag "regional" :fn ais-types/x}
  {:len  1 :desc "DTE"                      :tag "dte"      :fn ais-types/b}
  {:len  3 :desc "Spare"                    :tag "spare"    :fn ais-types/x}
  {:len  1 :desc "Assigned"                 :tag "assigned" :fn ais-types/b}
  {:len  1 :desc "RAIM flag"                :tag "raim"     :fn ais-types/b}
  {:len 20 :desc "Radio status"             :tag "radio"    :fn ais-types/u}
))
