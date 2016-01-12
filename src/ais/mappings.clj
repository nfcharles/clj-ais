(ns ais.mappings
  (:require [clojure.string :as string])
  (:require [ais.vocab :as ais-vocab])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:gen-class))


(def tag-block (hash-map
  "c" { :desc "Timestamp" 
        :tag  "timestamp" 
        :ex   ais-ex/extract-timestamp 
        :fn   #(ais-util/timestamp->iso (* 1000 (read-string %))) } 
  "s" { :desc "Source"
        :tag  "station"
        :ex   ais-ex/extract-source
        :fn   #(identity %)} 
  "n" { :desc "Line"
        :tag  "line"
        :ex   ais-ex/extract-line
        :fn   #(read-string %) } ))

(defn- rot-sq [x]
  (let [factor (if (< x 0) -1 1)]
    (int (* factor (* x x)))))

(def base-mapping (list
  {:len  2 :desc "Repeat Indicator"         :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                     :tag "mmsi"     :fn ais-types/u}
  {:len  4 :desc "Navigation Status"        :tag "status"   :fn (partial ais-types/e ais-vocab/navigation-status)}
  {:len  8 :desc "Rate of Turn (ROT)"       :tag "turn"     :fn (partial ais-types/I (/ 1.0 4.733) 3 rot-sq)}
  {:len 10 :desc "Speed Over Ground (SOG)"  :tag "speed"    :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len  1 :desc "Position Accuracy"        :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longtitude"               :tag "lon"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len 27 :desc "Latitude"                 :tag "lat"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len 12 :desc "Course Over Ground (COG)" :tag "course"   :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len  9 :desc "True Heading (HDG)"       :tag "heading"  :fn ais-types/u}
  {:len  6 :desc "Time Stamp"               :tag "second"   :fn ais-types/u}
  {:len  2 :desc "Maneuver Indicator"       :tag "maneuver" :fn (partial ais-types/e ais-vocab/maneuver-indicator)}
  {:len  3 :desc "Spare"                    :tag "spare"    :fn ais-types/x}
  {:len  1 :desc "RAIM flag"                :tag "raim"     :fn ais-types/b}
  {:len 19 :desc "Radio status"             :tag "radio"    :fn ais-types/u}
))

(def mapping-4 (list
  {:len  2 :desc "Repeat Indicator"       :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                   :tag "mmsi"     :fn ais-types/u}
  {:len 14 :desc "Year (UTC)"             :tag "year"     :fn ais-types/u}
  {:len  4 :desc "Month (UTC)"            :tag "month"    :fn ais-types/u}
  {:len  5 :desc "Day (UTC)"              :tag "day"      :fn ais-types/u}
  {:len  5 :desc "Hour (UTC)"             :tag "hour"     :fn ais-types/u}
  {:len  6 :desc "Minute (UTC)"           :tag "minute"   :fn ais-types/u}
  {:len  6 :desc "Second (UTC)"           :tag "second"   :fn ais-types/u}
  {:len  1 :desc "Fix quality"            :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longtitude"             :tag "lon"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len 27 :desc "Latitude"               :tag "lat"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len  4 :desc "Type of EPFD"           :tag "epfd"     :fn (partial ais-types/e ais-vocab/position-fix-type)}  
  {:len 10 :desc "Spare"                  :tag "spare"    :fn ais-types/x}
  {:len  1 :desc "RAIM flag"              :tag "raim"     :fn ais-types/b}
  {:len 19 :desc "SOTDMA state"           :tag "radio"    :fn ais-types/u}
))

(def mapping-5 (list
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                   :tag "mmsi"         :fn ais-types/u}
  {:len   2 :desc "AIS Version"            :tag "ais_version"  :fn ais-types/u}
  {:len  30 :desc "IMO Number"             :tag "imo"          :fn ais-types/u}
  {:len  42 :desc "Call Sign"              :tag "callsign"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 7)}
  {:len 120 :desc "Vessel Name"            :tag "shipname"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   8 :desc "Ship Type"              :tag "shiptype"     :fn (partial ais-types/e ais-vocab/ship-type)}
  {:len   9 :desc "Dimension to Bow"       :tag "to_bow"       :fn ais-types/u}
  {:len   9 :desc "Dimension to Stern"     :tag "to_stern"     :fn ais-types/u}
  {:len   6 :desc "Dimension to Port"      :tag "to_port"      :fn ais-types/u}
  {:len   6 :desc "Dimension to Starboard" :tag "to_starboard" :fn ais-types/u}
  {:len   4 :desc "Position Fix Type"      :tag "epfd"         :fn (partial ais-types/e ais-vocab/position-fix-type)}
  {:len   4 :desc "ETA month (UTC)"        :tag "month"        :fn ais-types/u}
  {:len   5 :desc "ETA day (UTC)"          :tag "day"          :fn ais-types/u}
  {:len   5 :desc "ETA hour (UTC)"         :tag "hour"         :fn ais-types/u}
  {:len   6 :desc "ETA minute (UTC)"       :tag "minute"       :fn ais-types/u}
  {:len   8 :desc "Draught"                :tag "draught"      :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len 120 :desc "Destination"            :tag "destination"  :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   1 :desc "DTE"                    :tag "dte"          :fn ais-types/b}
  {:len   1 :desc "Spare"                  :tag "spare"        :fn ais-types/x}
))

;; up to 5 AIVDM sentance payloads
(def mapping-6 (list 
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"     :fn ais-types/u}
  {:len  30 :desc "SourceMMSI"             :tag "mmsi"       :fn ais-types/u}
  {:len   2 :desc "Sequence Number"        :tag "seqno"      :fn ais-types/u}
  {:len  30 :desc "Destination MMSI"       :tag "dest_mmsi"  :fn ais-types/u}
  {:len   1 :desc "Retransmit flag"        :tag "retransmit" :fn ais-types/b}
  {:len   1 :desc "Spare"                  :tag "spare"      :fn ais-types/x}
  {:len  10 :desc "Designated Area Code"   :tag "dac"        :fn ais-types/u}
  {:len   6 :desc "Functional ID"          :tag "fid"        :fn ais-types/u}
  {:len 920 :desc "Data"                   :tag "data"       :fn ais-types/d} ; d handler not implemented
))

(def msg-spec {
 1 base-mapping
 2 base-mapping
 3 base-mapping
 4 mapping-4
 5 mapping-5
})
