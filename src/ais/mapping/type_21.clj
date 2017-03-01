(ns ais.mapping.type_21
  (:require [clojure.string :as string])
  (:require [ais.vocab :as ais-vocab])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mapping.common :as ais-map-comm])
  (:gen-class))


(def mapping-21 (list
  {:len   6 :desc "Message Type"             :tag "type"         :fn (partial ais-map-comm/const 21)}
  {:len   2 :desc "Repeat Indicator"         :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                     :tag "mmsi"         :fn ais-types/u}
  {:len   5 :desc "Aid type"                 :tag "aid_type"     :fn (partial ais-types/e ais-vocab/nav-aid-type)}
  {:len 120 :desc "Name"                     :tag "name"         :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   1 :desc "Position Accuracy"        :tag "accuracy"     :fn ais-types/b}
  {:len  28 :desc "Longtitude"               :tag "lon"          :fn ais-map-comm/lon}
  {:len  27 :desc "Latitude"                 :tag "lat"          :fn ais-map-comm/lat}
  {:len   9 :desc "Dimension to Bow"         :tag "to_bow"       :fn ais-types/u}
  {:len   9 :desc "Dimension to Stern"       :tag "to_stern"     :fn ais-types/u}
  {:len   6 :desc "Dimension to Port"        :tag "to_port"      :fn ais-types/u}
  {:len   6 :desc "Dimension to Starboard"   :tag "to_starboard" :fn ais-types/u}
  {:len   4 :desc "Type of EPFD"             :tag "epfd"         :fn (partial ais-types/e ais-vocab/position-fix-type)}
  {:len   6 :desc "UTC second"               :tag "second"       :fn ais-types/u}
  {:len   1 :desc "Off-Position Indicator"   :tag "off_position" :fn ais-types/b}
  {:len   8 :desc "Regional reserved"        :tag "regional"     :fn ais-types/u}
  {:len   1 :desc "RAIM flag"                :tag "raim"         :fn ais-types/b}
  {:len   1 :desc "Virtual-aid flag"         :tag "virtual_aid"  :fn ais-types/u}
  {:len   1 :desc "Assigned mode flag"       :tag "assigned"     :fn ais-types/u}
  {:len   1 :desc "Spare"                    :tag "spare"        :fn ais-types/x}
  {:len  88 :desc "Name"                     :tag "name_ext"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 14)}
))