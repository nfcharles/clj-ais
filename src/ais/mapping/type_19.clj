(ns ais.mapping.type_19
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-19 (list
  {:len   6 :desc "Message Type"             :tag "type"         :fn (partial common/const 19)}
  {:len   2 :desc "Repeat Indicator"         :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                     :tag "mmsi"         :fn ais-types/u}
  {:len   8 :desc "Regional Reserved"        :tag "reserved"     :fn ais-types/x}
  {:len  10 :desc "Speed Over Ground (SOG)"  :tag "speed"        :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len   1 :desc "Position Accuracy"        :tag "accuracy"     :fn ais-types/b}
  {:len  28 :desc "Longtitude"               :tag "lon"          :fn common/lon}
  {:len  27 :desc "Latitude"                 :tag "lat"          :fn common/lat}
  {:len  12 :desc "Course Over Ground (COG)" :tag "course"       :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len   9 :desc "True Heading (HDG)"       :tag "heading"      :fn ais-types/u}
  {:len   6 :desc "Time Stamp"               :tag "second"       :fn ais-types/u}
  {:len   4 :desc "Regional reserved"        :tag "regional"     :fn ais-types/u}
  {:len 120 :desc "Name"                     :tag "shipname"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   8 :desc "Typeofship and cargo"     :tag "shiptype"     :fn (partial ais-types/e ais-vocab/ship-type)}
  {:len   9 :desc "Dimension to Bow"         :tag "to_bow"       :fn ais-types/u}
  {:len   9 :desc "Dimension to Stern"       :tag "to_stern"     :fn ais-types/u}
  {:len   6 :desc "Dimension to Port"        :tag "to_port"      :fn ais-types/u}
  {:len   6 :desc "Dimension to Starboard"   :tag "to_starboard" :fn ais-types/u}
  {:len   4 :desc "Position Fix Type"        :tag "epfd"         :fn (partial ais-types/e ais-vocab/position-fix-type)}
  {:len   1 :desc "RAIM flag"                :tag "raim"         :fn ais-types/b}
  {:len   1 :desc "DTE"                      :tag "dte"          :fn ais-types/b}
  {:len   1 :desc "Assigned mode flag"       :tag "assigned"     :fn ais-types/u}
  {:len   4 :desc "Spare"                    :tag "spare"        :fn ais-types/x}
))
