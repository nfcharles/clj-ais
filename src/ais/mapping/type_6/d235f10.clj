(ns ais.mapping.type_6.d235f10
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def d235f10 (list
  {:len 10 :desc "Analogue"           :tag "ana_int"  :fn ais-types/u}
  {:len 10 :desc "Analogue (ext. #1)" :tag "ana_ext1" :fn ais-types/u}
  {:len 10 :desc "Analogue (ext. #2)" :tag "ana_ext2" :fn ais-types/u}
  {:len  2 :desc "RACON Status"       :tag "racon"    :fn ais-types/u}
  {:len  2 :desc "Light Status"       :tag "light"    :fn ais-types/u}
  {:len  1 :desc "Health"             :tag "health"   :fn ais-types/b}
  {:len  8 :desc "Status (external)"  :tag "stat_ext" :fn ais-types/u}
  {:len  1 :desc "Position Status"    :tag "off_pos"  :fn ais-types/b}
  {:len  4 :desc "Spare"              :tag "spare"    :fn ais-types/x}
))