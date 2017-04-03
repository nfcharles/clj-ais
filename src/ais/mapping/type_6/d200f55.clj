(ns ais.mapping.type_6.d200f55
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def d200f55 (list
  {:len  8 :desc "# Crew on Board"       :tag "crew"       :fn ais-types/u}
  {:len 13 :desc "# Passengers on Board" :tag "passengers" :fn ais-types/u}
  {:len  8 :desc "# Personnel on Board"  :tag "personnel"  :fn ais-types/u}
  {:len 51 :desc "Spare"                 :tag "spare"      :fn ais-types/x}
))
