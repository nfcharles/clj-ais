(ns ais.mapping.type_27
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-27 (list
  {:len  6 :desc "Message Type"         :tag "type"     :fn (partial common/const 27)}
  {:len  2 :desc "Repeat Indicator"     :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                 :tag "mmsi"     :fn ais-types/u}
  {:len  1 :desc "Position Accuracy"    :tag "accuracy" :fn ais-types/u}
  {:len  1 :desc "RAIM flag"            :tag "raim"     :fn ais-types/u}
  {:len  4 :desc "Navigation Status"    :tag "status"   :fn (partial ais-types/e ais-vocab/navigation-status)}
  {:len 18 :desc "Longitude"            :tag "lon"      :fn (partial ais-types/I (/ 1.0 600) 4)}
  {:len 17 :desc "Latitude"             :tag "lat"      :fn (partial ais-types/I (/ 1.0 600) 4)}
  {:len  6 :desc "Speed Over Ground"    :tag "speed"    :fn ais-types/u}
  {:len  9 :desc "Course Over Ground"   :tag "course"   :fn ais-types/u}
  {:len  1 :desc "GNSS Position Status" :tag "gnss"     :fn ais-types/u}
  {:len  1 :desc "Spare"                :tag "spare"    :fn ais-types/x}
))
