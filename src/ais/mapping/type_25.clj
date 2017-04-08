(ns ais.mapping.type_25
  (:require [ais.types :as ais-types]
            [ais.util :as ais-util]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-25-base  (list
  {:len  6 :desc "Message Type"          :tag "type"       :fn (partial common/const 25)}
  {:len  2 :desc "Repeat Indicator"      :tag "repeat"     :fn ais-types/u}
  {:len 30 :desc "MMSI"                  :tag "mmsi"       :fn ais-types/u}
  {:len  1 :desc "Destination Indicator" :tag "addressed"  :fn ais-types/b}
  {:len  1 :desc "Binary Data Flag"      :tag "structured" :fn ais-types/b}
))

(def addressed (list
  {:len  30 :desc "Destination MMSI" :tag "dest_mmsi" :fn ais-types/u}
  {:len 128 :desc "Data"             :tag "data"      :fn ais-types/d}
))

(def application-id (list
  {:len  10 :desc "DAC"  :tag "dac"  :fn ais-types/u}
  {:len   6 :desc "FID"  :tag "fid"  :fn ais-types/u}
  {:len 128 :desc "Data" :tag "data" :fn ais-types/d}
))

(def default (list
  {:len 128 :desc "Data"          :tag "data"   :fn ais-types/d}
))

(defn determine-25-map [bits]
  (println (format "BIT-LEN: %d" (count bits)))
  (let [addressed (ais-types/b nil (subs bits 38 39))
        structured (ais-types/b nil (subs bits 39 40))]
    (cond
      addressed  (concat mapping-25-base addressed)
      structured (concat mapping-25-base application-id)
      :else      (concat mapping-25-base default))))
