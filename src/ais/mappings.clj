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


(def mapping-18 (list
  {:len  2 :desc "Repeat Indicator"         :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                     :tag "mmsi"     :fn ais-types/u}
  {:len  8 :desc "Regional Reserved"        :tag "reserved" :fn ais-types/x}
  {:len 10 :desc "Speed Over Ground (SOG)"  :tag "speed"    :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len  1 :desc "Position Accuracy"        :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longtitude"               :tag "lon"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len 27 :desc "Latitude"                 :tag "lat"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
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


(def mapping-19 (list
  {:len   2 :desc "Repeat Indicator"         :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                     :tag "mmsi"         :fn ais-types/u}
  {:len   8 :desc "Regional Reserved"        :tag "reserved"     :fn ais-types/x}
  {:len  10 :desc "Speed Over Ground (SOG)"  :tag "speed"        :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len   1 :desc "Position Accuracy"        :tag "accuracy"     :fn ais-types/b}
  {:len  28 :desc "Longtitude"               :tag "lon"          :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len  27 :desc "Latitude"                 :tag "lat"          :fn (partial ais-types/I (/ 1.0 600000) 4)}
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

(def mapping-21 (list
  {:len   2 :desc "Repeat Indicator"         :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                     :tag "mmsi"         :fn ais-types/u}
  {:len   5 :desc "Aid type"                 :tag "aid_type"     :fn (partial ais-types/e ais-vocab/nav-aid-type)}
  {:len 120 :desc "Name"                     :tag "name"         :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   1 :desc "Position Accuracy"        :tag "accuracy"     :fn ais-types/b}
  {:len  28 :desc "Longtitude"               :tag "lon"          :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len  27 :desc "Latitude"                 :tag "lat"          :fn (partial ais-types/I (/ 1.0 600000) 4)}
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

;;; ---
;;; Type 24 message has non-trivial parsing rules
;;; 
;;;          message
;;;             |
;;;             | --> part number (0 | 1) ?
;;;             |
;;;           /   \   
;;;       0  /     \  1
;;;         /       \
;;;       24-a    24-b-*
;;;                 |
;;;                 | --> auxilary vessel ?
;;;                 |
;;;               /   \
;;;        yes   /     \    no
;;;             /       \
;;;          24-b-1   24-b-2
;;; ---
;;;
;;; 24-b-* determines the interpreation of the penultimate block, the last 30 bits before the spare bits block.
;;; ---
;;; 24-b-1
;;;   * The 30 bits are interpreted as the mmsi of the auxilary vessel's mother vessel
;;;
;;; 24-b-2
;;;   * The 30 bits are interpreted as dimensions of the vessel
 
(def mapping-24-a (list
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                   :tag "mmsi"         :fn ais-types/u}
  {:len   2 :desc "Part Number"            :tag "partno"       :fn ais-types/u}
  {:len 120 :desc "Vessel Name"            :tag "shipname"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   8 :desc "Spare"                  :tag "spare"        :fn ais-types/x}
))

;; last 30 bits before spare are interpreted as vessel dimensions
(def mapping-24-b-dim (list
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"          :fn ais-types/u}
  {:len  30 :desc "MMSI"                   :tag "mmsi"            :fn ais-types/u}
  {:len   2 :desc "Part Number"            :tag "partno"          :fn ais-types/u}
  {:len   8 :desc "Ship Type"              :tag "shiptype"        :fn (partial ais-types/e ais-vocab/ship-type)}
  {:len  18 :desc "Vendor ID"              :tag "vendorid"        :fn (partial ais-types/t ais-vocab/sixbit-ascii 3)}
  {:len   4 :desc "Unit Model Code"        :tag "model"           :fn ais-types/u}
  {:len  20 :desc "Serial Number"          :tag "serial"          :fn ais-types/u}
  {:len  42 :desc "Call Sign"              :tag "callsign"        :fn (partial ais-types/t ais-vocab/sixbit-ascii 7)}
  {:len   9 :desc "Dimension to Bow"       :tag "to_bow"          :fn ais-types/u}
  {:len   9 :desc "Dimension to Stern"     :tag "to_stern"        :fn ais-types/u}
  {:len   6 :desc "Dimension to Port"      :tag "to_port"         :fn ais-types/u}
  {:len   6 :desc "Dimension to Starboard" :tag "to_starboard"    :fn ais-types/u}  
  {:len   6 :desc "Spare"                  :tag "spare"           :fn ais-types/x}
))

;; last 30 bits before spare are interpreted as vessel parent mmsi
(def mapping-24-b-mmsi (list
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"          :fn ais-types/u}
  {:len  30 :desc "MMSI"                   :tag "mmsi"            :fn ais-types/u}
  {:len   2 :desc "Part Number"            :tag "partno"          :fn ais-types/u}
  {:len   8 :desc "Ship Type"              :tag "shiptype"        :fn (partial ais-types/e ais-vocab/ship-type)}
  {:len  18 :desc "Vendor ID"              :tag "vendorid"        :fn (partial ais-types/t ais-vocab/sixbit-ascii 3)}
  {:len   4 :desc "Unit Model Code"        :tag "model"           :fn ais-types/u}
  {:len  20 :desc "Serial Number"          :tag "serial"          :fn ais-types/u}
  {:len  42 :desc "Call Sign"              :tag "callsign"        :fn (partial ais-types/t ais-vocab/sixbit-ascii 7)}
  {:len  30 :desc "Mothership MMSI"        :tag "mothership_mmsi" :fn ais-types/u}
  {:len   6 :desc "Spare"                  :tag "spare"           :fn ais-types/x}
))

(defn- determine-b-map [bits]
  (let [mmsi-prefix (Math/floor (/ (ais-types/u (subs bits 2 32)) 10000000))]
    (if (= 98.0 mmsi-prefix) ; if auxilary mmsi signature, use b-mmsi spec otherwise b-dim spec
      mapping-24-b-mmsi
      mapping-24-b-dim)))
      
(defn- determine-map [bits]
  (let [part-no (ais-types/u (subs bits 32 34))]
    (case part-no
      0 mapping-24-a
      1 (determine-b-map bits)
      nil)))


;; ---
;; Map selection functions
;; --

(def msg-spec {
  1 base-mapping
  2 base-mapping
  3 base-mapping
  4 mapping-4
  5 mapping-5
 18 mapping-18
 19 mapping-19
 21 mapping-21
})

(defmulti select-map 
  (fn [msg-type bits] msg-type))

(defmethod select-map 24 [_ bits]
  (determine-map bits))

;; TODO: Maybe more efficient to have method for each defined type rather
;; than defaulting to default case.  ???
(defmethod select-map :default [msg-type bits]
  (msg-spec msg-type))
