(ns ais.mapping.type_8.core
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
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

(defn determine-8-map [bits]
  (let [dac (ais-types/u nil (subs bits 40 50))
        fid (ais-types/u nil (subs bits 50 56))]
    (println (format "dac: %3d, fid: %3d" dac fid))
    mapping-8-generic))
