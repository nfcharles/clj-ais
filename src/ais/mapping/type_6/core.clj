(ns ais.mapping.type_6.core
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common]
	    [ais.mapping.type_6.d1f12   :as d1f12]
	    [ais.mapping.type_6.d1f14   :as d1f14]
	    [ais.mapping.type_6.d1f16   :as d1f16]
	    [ais.mapping.type_6.d1f18   :as d1f18]
            [ais.mapping.type_6.d1f23   :as d1f23]
            [ais.mapping.type_6.d200f21 :as d200f21]
            [ais.mapping.type_6.d200f22 :as d200f22]
            [ais.mapping.type_6.d200f55 :as d200f55]
            [ais.mapping.type_6.d235f10 :as d235f10])
  (:gen-class))


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

(def DAC_FID-mapping (hash-map
    1 { 12 { :desc "Dangerous cargo indication"   :map (concat mapping-6-base d1f12/d1f12) }
        14 { :desc "Tidal window"                 :map (concat mapping-6-base d1f14/d1f14) }
        16 { :desc "Num persons on board"         :map (concat mapping-6-base d1f16/d1f16) }
        18 { :desc "Clearance time to enter port" :map (concat mapping-6-base d1f18/d1f18) }
        20 { :desc "Berthing data (addressed)"    :map mapping-6-generic }
        23 { :desc "Area notice (addressed)"      :map (concat mapping-6-base d1f23/d1f23) }
        25 { :desc "Dangerous cargo indication"   :map mapping-6-generic }
        28 { :desc "Route info addressed"         :map mapping-6-generic }
        30 { :desc "Text description addressed"   :map mapping-6-generic }
        32 { :desc "Tidal window"                 :map mapping-6-generic }}

  200 { 21 { :desc "ETA at lock/bridge/terminal"  :map (concat mapping-6-base d200f21/d200f21) }
        22 { :desc "RTA at lock/bridge/terminal"  :map (concat mapping-6-base d200f22/d200f22) }
        55 { :desc "Number of persons on board"   :map (concat mapping-6-base d200f55/d200f55) }}

  235 { 10 { :desc "AtoN monitoring data (UK)"    :map (concat mapping-6-base d235f10/d235f10) }}

  250 { 10 { :desc "AtoN monitoring data (ROI)"   :map (concat mapping-6-base d235f10/d235f10) }}))


(defn determine-6-map [bits]
  (let [dac (ais-types/u nil (subs bits 72 82))
        fid (ais-types/u nil (subs bits 82 88))]
    (println (format "dac: %3d, fid: %3d" dac fid))
    (if-let [fid-map (DAC_FID-mapping dac)]
      (if-let [ret (fid-map fid)]
        (ret :map)
        mapping-6-generic)
      mapping-6-generic)))
