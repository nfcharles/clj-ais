(ns ais.mapping.type_6.core
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common]
	    [ais.mapping.type_6.f1d12 :as f1d12]
	    [ais.mapping.type_6.f1d14 :as f1d14]
	    [ais.mapping.type_6.f1d16 :as f1d16]
	    [ais.mapping.type_6.f1d18 :as f1d18]
            [ais.mapping.type_6.f1d23 :as f1d23])
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


(def DAC_FID-mapping (hash-map
    1 { 12 { :desc "Dangerous cargo indication"   :map (cons mapping-6-base f1d12/f1d12) }
        14 { :desc "Tidal window"                 :map (cons mapping-6-base f1d14/f1d14) }
        16 { :desc "Num persons on board"         :map (cons mapping-6-base f1d16/f1d16) }
        18 { :desc "Clearance time to enter port" :map (cons mapping-6-base f1d18/f1d18) }
        20 { :desc "Berthing data (addressed)"    :map mapping-6-generic }
        23 { :desc "Area notice (addressed)"      :map (cons mapping-6-base f1d23/f1d23) }
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
  (let [dac (ais-types/u nil (subs bits 72 82))
        fid (ais-types/u nil (subs bits 82 88))]
    (println (format "dac: %d, fid: %d" dac fid))
    (if-let [fid-map (DAC_FID-mapping dac)]
      (if-let [ret (fid-map fid)]
        (ret :map)
        mapping-6-generic)
      mapping-6-generic)))