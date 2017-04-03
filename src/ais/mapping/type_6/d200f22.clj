(ns ais.mapping.type_6.d200f22
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def d200f22 (list
  {:len 12 :desc "UN Country Code"    :tag "country"    :fn (partial ais-types/t ais-vocab/sixbit-ascii 2)}
  {:len 18 :desc "UN/LOCODE"          :tag "locode"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 3)}
  {:len 30 :desc "Fairway Section"    :tag "section"    :fn (partial ais-types/t ais-vocab/sixbit-ascii 5)}
  {:len 30 :desc "Terminal Code"      :tag "terminal"   :fn (partial ais-types/t ais-vocab/sixbit-ascii 5)}
  {:len 30 :desc "Fairway hectometre" :tag "hectometre" :fn (partial ais-types/t ais-vocab/sixbit-ascii 5)}
  {:len  4 :desc "RTA month"          :tag "month"      :fn ais-types/u}
  {:len  5 :desc "RTA day"            :tag "day"        :fn ais-types/u}
  {:len  5 :desc "RTA hour"           :tag "hour"       :fn ais-types/u}
  {:len  6 :desc "RTA minute"         :tag "minute"     :fn ais-types/u}
  {:len  2 :desc "Status"             :tag "status"     :fn (partial ais-types/e ais-vocab/lbt-status-code)}
  {:len  2 :desc "Spare"              :tag "spare"      :fn ais-types/x}
))
