(ns ais.mapping.type_6.f1d12
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def f1d12 (list
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
