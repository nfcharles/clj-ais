(ns ais.mapping.type_8.core
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
	    [ais.mapping.type_8.d1f31 :as d1f31]
	    [ais.mapping.type_8.d200f10 :as d200f10]
	    [ais.mapping.type_8.d200f24 :as d200f24]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-8-generic (list
  {:len   6 :desc "Message Type"         :tag "type"   :fn (partial common/const 8)}
  {:len   2 :desc "Repeat Indicator"     :tag "repeat" :fn ais-types/u}
  {:len  30 :desc "Source MMSI"          :tag "mmsi"   :fn ais-types/u}
  {:len   2 :desc "Spare"                :tag "spare"  :fn ais-types/x}
  {:len  10 :desc "Designated Area Code" :tag "dac"    :fn ais-types/u}
  {:len   6 :desc "Functional ID"        :tag "fid"    :fn ais-types/u}
  {:len 952 :desc "Data"                 :tag "data"   :fn ais-types/d}
))

(def mapping-8-base (list
  {:len   6 :desc "Message Type"         :tag "type"   :fn (partial common/const 8)}
  {:len   2 :desc "Repeat Indicator"     :tag "repeat" :fn ais-types/u}
  {:len  30 :desc "Source MMSI"          :tag "mmsi"   :fn ais-types/u}
  {:len   2 :desc "Spare"                :tag "spare"  :fn ais-types/x}
  {:len  10 :desc "Designated Area Code" :tag "dac"    :fn ais-types/u}
  {:len   6 :desc "Functional ID"        :tag "fid"    :fn ais-types/u}
))

(def DAC_FID-mapping (hash-map
    1 { 31 { :desc "Meteorological and Hydrological Data"  :map (concat mapping-8-base d1f31/d1f31) }}
  200 { 10 { :desc "Ship Static and Voyage Related Data"   :map (concat mapping-8-base d200f10/d200f10) }}
  200 { 24 { :desc "Water Levels"                          :map (concat mapping-8-base d200f24/d200f24) }}
))

(defn determine-8-map [bits]
  (let [dac (ais-types/u nil (subs bits 40 50))
        fid (ais-types/u nil (subs bits 50 56))]
    (println (format "dac: %3d, fid: %3d" dac fid))
    (if-let [fid-map (DAC_FID-mapping dac)]
      (if-let [ret (fid-map fid)]
        (ret :map)
        mapping-8-generic)
      mapping-8-generic)))
