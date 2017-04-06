(ns ais.mapping.type_8.d200f10
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def d200f10 (list
  {:len 48 :desc "European Vessel ID"    :tag "vin"       :fn (partial ais-types/t ais-vocab/sixbit-ascii 8)}
  {:len 13 :desc "Length of Ship"        :tag "length"    :fn ais-types/u}
  {:len 10 :desc "Beam of Ship"          :tag "beam"      :fn ais-types/u}
  {:len 14 :desc "Ship/Combination Type" :tag "shiptype"  :fn (partial ais-types/e ais-vocab/eri-classification-code)}
  {:len  3 :desc "Hazardous Cargo"       :tag "hazard"    :fn (partial ais-types/e ais-vocab/hazard-code)}
  {:len 11 :desc "Draught"               :tag "draught"   :fn ais-types/u}
  {:len  2 :desc "Loaded/Unloaded"       :tag "loaded"    :fn (partial ais-types/e ais-vocab/load-status)}
  {:len  1 :desc "Speed Inf. Quality"    :tag "speed_q"   :fn ais-types/b}
  {:len  1 :desc "Course Inf. Quality"   :tag "course_q"  :fn ais-types/b}
  {:len  1 :desc "Heading Inf. Quality"  :tag "heading_q" :fn ais-types/b}
  {:len  8 :desc "Spare"                 :tag "spare"     :fn ais-types/u}
))
