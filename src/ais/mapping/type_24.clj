(ns ais.mapping.type_24
  (:require [clojure.string :as string]
            [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.util :as ais-util]
            [ais.extractors :as ais-ex]
            [ais.mapping.common :as ais-map-comm])
  (:gen-class))


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
  {:len   6 :desc "Message Type"           :tag "type"         :fn (partial ais-map-comm/const 24)}
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                   :tag "mmsi"         :fn ais-types/u}
  {:len   2 :desc "Part Number"            :tag "partno"       :fn ais-types/u}
  {:len 120 :desc "Vessel Name"            :tag "shipname"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   8 :desc "Spare"                  :tag "spare"        :fn ais-types/x}
))

;; last 30 bits before spare are interpreted as vessel dimensions
(def mapping-24-b-dim (list
  {:len   6 :desc "Message Type"           :tag "type"            :fn (partial ais-map-comm/const 24)}
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
  {:len   6 :desc "Message Type"           :tag "type"            :fn (partial ais-map-comm/const 24)}
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

(defn- determine-24-b-map [bits]
  (let [mmsi-prefix (Math/floor (/ (ais-types/u nil (subs bits 2 32)) 10000000))]
    (if (= 98.0 mmsi-prefix) ; check for auxilary mmsi signature (98XXXXXXX)
      mapping-24-b-mmsi
      mapping-24-b-dim)))
      
(defn determine-24-map [bits]
  (let [part-no (ais-types/u nil (subs bits 38 40))]
    (case (int part-no)
      0 mapping-24-a
      1 (determine-24-b-map bits)
      nil)))
