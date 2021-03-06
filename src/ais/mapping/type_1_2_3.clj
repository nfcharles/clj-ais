(ns ais.mapping.type_1_2_3
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(defn- rot-sq [x]
  (let [factor (if (< x 0) -1 1)]
    (int (* factor (* x x)))))

(def base-mapping (list
  {:len  2 :desc "Repeat Indicator"         :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                     :tag "mmsi"     :fn ais-types/u}
  {:len  4 :desc "Navigation Status"        :tag "status"   :fn (partial ais-types/e ais-vocab/navigation-status)}
  {:len  8 :desc "Rate of Turn (ROT)"       :tag "turn"     :fn (partial ais-types/I (/ 1.0 4.733) 3 rot-sq)}
  {:len 10 :desc "Speed Over Ground (SOG)"  :tag "speed"    :fn common/speed}
  {:len  1 :desc "Position Accuracy"        :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longitude"                :tag "lon"      :fn common/lon}
  {:len 27 :desc "Latitude"                 :tag "lat"      :fn common/lat}
  {:len 12 :desc "Course Over Ground (COG)" :tag "course"   :fn common/course}
  {:len  9 :desc "True Heading (HDG)"       :tag "heading"  :fn ais-types/u}
  {:len  6 :desc "Time Stamp"               :tag "second"   :fn ais-types/u}
  {:len  2 :desc "Maneuver Indicator"       :tag "maneuver" :fn (partial ais-types/e ais-vocab/maneuver-indicator)}
  {:len  3 :desc "Spare"                    :tag "spare"    :fn ais-types/x}
  {:len  1 :desc "RAIM flag"                :tag "raim"     :fn ais-types/b}
  {:len 19 :desc "Radio status"             :tag "radio"    :fn ais-types/u}
))

(def mapping-1 
  (cons {:len 6 :desc "Message Type" :tag "type" :fn (partial common/const 1)} base-mapping))

(def mapping-2
  (cons {:len 6 :desc "Message Type" :tag "type" :fn (partial common/const 2)} base-mapping))

(def mapping-3 
  (cons {:len 6 :desc "Message Type" :tag "type" :fn (partial common/const 3)} base-mapping))
