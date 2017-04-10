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

(defn addr-map [len]
  (concat
    mapping-25-base
    (list
      {:len  30 :desc "Destination MMSI" :tag "dest_mmsi" :fn ais-types/u}
      {:len len :desc "Data"             :tag "data"      :fn ais-types/d})))

(defn strct-map [len]
  (concat
    mapping-25-base
    (list
      {:len  10 :desc "DAC"  :tag "dac"  :fn ais-types/u}
      {:len   6 :desc "FID"  :tag "fid"  :fn ais-types/u}
      {:len len :desc "Data" :tag "data" :fn ais-types/d})))

(defn addr-strct-map [len]
  (concat
    mapping-25-base
    (list
      {:len  30 :desc "Destination MMSI" :tag "dest_mmsi" :fn ais-types/u}
      {:len  10 :desc "DAC"              :tag "dac"       :fn ais-types/u}
      {:len   6 :desc "FID"              :tag "fid"       :fn ais-types/u}
      {:len len :desc "Data"             :tag "data"      :fn ais-types/d})))

(defn default-map [len]
  (concat
    mapping-25-base
    (list
      {:len len :desc "Data" :tag "data" :fn ais-types/d})))

(defn determine-25-map [bits]
  (let [addr (ais-types/b nil (subs bits 38 39))
        strct (ais-types/b nil (subs bits 39 40))]
    (cond
      (and addr strct) (addr-strct-map (min (- (count bits) 86) 128))
      addr (addr-map (min (- (count bits) 70) 128))
      strct (strct-map (min (- (count bits) 56) 128))
      :else (default-map (min (- (count bits) 40) 128)))))
