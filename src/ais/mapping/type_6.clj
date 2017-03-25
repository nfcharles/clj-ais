(ns ais.mapping.type_6
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

;; up to 5 AIVDM sentance payloads
(def mapping-6-generic (list 
  {:len   6 :desc "Message Type"           :tag "type"       :fn (partial common/const 6)}
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"     :fn ais-types/u}
  {:len  30 :desc "SourceMMSI"             :tag "mmsi"       :fn ais-types/u}
  {:len   2 :desc "Sequence Number"        :tag "seqno"      :fn ais-types/u}
  {:len  30 :desc "Destination MMSI"       :tag "dest_mmsi"  :fn ais-types/u}
  {:len   1 :desc "Retransmit flag"        :tag "retransmit" :fn ais-types/b}
  {:len   1 :desc "Spare"                  :tag "spare"      :fn ais-types/x}
  {:len  10 :desc "Designated Area Code"   :tag "dac"        :fn ais-types/u}
  {:len   6 :desc "Functional ID"          :tag "fid"        :fn ais-types/u}
  {:len 920 :desc "Data"                   :tag "data"       :fn ais-types/d}
))

(def mapping-6-base (list 
  {:len   6 :desc "Message Type"           :tag "type"       :fn (partial common/const 6)}
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"     :fn ais-types/u}
  {:len  30 :desc "SourceMMSI"             :tag "mmsi"       :fn ais-types/u}
  {:len   2 :desc "Sequence Number"        :tag "seqno"      :fn ais-types/u}
  {:len  30 :desc "Destination MMSI"       :tag "dest_mmsi"  :fn ais-types/u}
  {:len   1 :desc "Retransmit flag"        :tag "retransmit" :fn ais-types/b}
  {:len   1 :desc "Spare"                  :tag "spare"      :fn ais-types/x}
  {:len  10 :desc "Designated Area Code"   :tag "dac"        :fn ais-types/u}
  {:len   6 :desc "Functional ID"          :tag "fid"        :fn ais-types/u}
))

(def subtype_1_12 (list
  {:len  30 :desc "Last Port Of Call"   :tag "lastport" :fn (partial ais-types/t ais-vocab/sixbit-ascii 5)}
  {:len   4 :desc "ETA month (UTC)"     :tag "lmonth"   :fn ais-types/u}
  {:len   5 :desc "ETA day (UTC)"       :tag "lday"     :fn ais-types/u}
  {:len   5 :desc "ETA hour (UTC)"      :tag "lhour"    :fn ais-types/u}
  {:len   6 :desc "ETA minute (UTC)"    :tag "lminute"  :fn ais-types/u}
  {:len  30 :desc "Next Port Of Call"   :tag "nextport" :fn (partial ais-types/t ais-vocab/sixbit-ascii 5)}
  {:len   4 :desc "ETA month (UTC)"     :tag "nmonth"   :fn ais-types/u}
  {:len   5 :desc "ETA day (UTC)"       :tag "nday"     :fn ais-types/u}
  {:len   5 :desc "ETA hour (UTC)"      :tag "nhour"    :fn ais-types/u}
  {:len   6 :desc "ETA minute (UTC)"    :tag "nminute"  :fn ais-types/u}
  {:len 120 :desc "Main Dangerous Good" :tag "nextport" :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len  24 :desc "IMD Category"        :tag "imdcat"   :fn (partial ais-types/t ais-vocab/sixbit-ascii 4)}
  {:len  13 :desc "UN Number"           :tag "unid"     :fn ais-types/u}
  {:len  10 :desc "Amount of Cargo"     :tag "amount"   :fn ais-types/u}
  {:len   2 :desc "Unit of Quantity"    :tag "unit"     :fn (partial ais-types/e ais-vocab/cargo-unit-code)}
  {:len   3 :desc "Spare"               :tag "spare"    :fn ais-types/x}
))

(def DAC-FID-mapping (hash-map
  1 { 12 { :desc "Dangerous cargo indication"   :map (cons mapping-6-base subtype_1_12) }
      14 { :desc "Tidal window"                 :map mapping-6-generic }
      16 { :desc "Num persons on board"         :map mapping-6-generic }
      18 { :desc "Clearance time to enter port" :map mapping-6-generic }
      20 { :desc "Berthing data (addressed)"    :map mapping-6-generic }
      23 { :desc "Area notice (addressed)"      :map mapping-6-generic }
      25 { :desc "Dangerous cargo indication"   :map mapping-6-generic }
      28 { :desc "Route info addressed"         :map mapping-6-generic }
      30 { :desc "Text description addressed"   :map mapping-6-generic }
      32 { :desc "Tidal window"                 :map mapping-6-generic }}

  200 { 21 { :desc "ETA at lock/bridge/terminal"  :map mapping-6-generic }
        22 { :desc "RTA at lock/bridge/terminal"  :map mapping-6-generic }
        55 { :desc "Number of persons on board"   :map mapping-6-generic }}
  
  235 { 10 { :desc "AtoN monitoring data (UK)"    :map mapping-6-generic }}

  250 { 10 { :desc "AtoN monitoring data (ROI)"   :map mapping-6-generic }}))


(defn determine-6-map [bits]
  (let [dac (ais-types/u (subs bits 72 82))
        fid (ais-types/u (subs bits 82 88))]
    (println (format "dac: %d, fid: %d" dac fid))
    (if-let [fid-map (DAC-FID-mapping dac)]
      (if-let [ret (fid-map fid)]
        (ret :map)
        mapping-6-generic)
      mapping-6-generic)))