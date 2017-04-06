(ns ais.mapping.type_18
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-18 (list
  {:len  6 :desc "Message Type"             :tag "type"     :fn (partial common/const 18)}
  {:len  2 :desc "Repeat Indicator"         :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                     :tag "mmsi"     :fn ais-types/u}
  {:len  8 :desc "Regional Reserved"        :tag "reserved" :fn ais-types/x}
  {:len 10 :desc "Speed Over Ground (SOG)"  :tag "speed"    :fn common/speed}
  {:len  1 :desc "Position Accuracy"        :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longitude"                :tag "lon"      :fn common/lon}
  {:len 27 :desc "Latitude"                 :tag "lat"      :fn common/lat}
  {:len 12 :desc "Course Over Ground (COG)" :tag "course"   :fn common/course}
  {:len  9 :desc "True Heading (HDG)"       :tag "heading"  :fn ais-types/u}
  {:len  6 :desc "Time Stamp"               :tag "second"   :fn ais-types/u}
  {:len  2 :desc "Regional reserved"        :tag "regional" :fn ais-types/u}
  {:len  1 :desc "CS Unit"                  :tag "cs"       :fn ais-types/b}
  {:len  1 :desc "Display flag"             :tag "display"  :fn ais-types/b}
  {:len  1 :desc "DSC Flag"                 :tag "dsc"      :fn ais-types/b}
  {:len  1 :desc "Band Flag"                :tag "band"     :fn ais-types/b}
  {:len  1 :desc "Message 22 flag"          :tag "msg22"    :fn ais-types/b}
  {:len  1 :desc "Assigned"                 :tag "assigned" :fn ais-types/b}
  {:len  1 :desc "RAIM flag"                :tag "raim"     :fn ais-types/b}
  {:len 20 :desc "Radio status"             :tag "radio"    :fn ais-types/u}
))