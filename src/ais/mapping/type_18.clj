(ns ais.mapping.type_18
  (:require [clojure.string :as string])
  (:require [ais.vocab :as ais-vocab])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mapping.common :as ais-map-comm])
  (:gen-class))


(def mapping-18 (list
  {:len  6 :desc "Message Type"             :tag "type"     :fn (partial ais-map-comm/const 18)}
  {:len  2 :desc "Repeat Indicator"         :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                     :tag "mmsi"     :fn ais-types/u}
  {:len  8 :desc "Regional Reserved"        :tag "reserved" :fn ais-types/x}
  {:len 10 :desc "Speed Over Ground (SOG)"  :tag "speed"    :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len  1 :desc "Position Accuracy"        :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longtitude"               :tag "lon"      :fn ais-map-comm/lon}
  {:len 27 :desc "Latitude"                 :tag "lat"      :fn ais-map-comm/lat}
  {:len 12 :desc "Course Over Ground (COG)" :tag "course"   :fn (partial ais-types/U (/ 1.0 10) 1)}
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