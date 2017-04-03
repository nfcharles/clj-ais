(ns ais.mapping.type_6.d1f18
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def d1f18 (list
  {:len  10 :desc "Message Linkage ID"   :tag "linkage"     :fn ais-types/u}
  {:len   4 :desc "Month (UTC)"          :tag "month"       :fn ais-types/u}
  {:len   5 :desc "Day (UTC)"            :tag "day"         :fn ais-types/u}
  {:len   5 :desc "Hour (UTC)"           :tag "hour"        :fn ais-types/u}
  {:len   6 :desc "Minute (UTC)"         :tag "minute"      :fn ais-types/u}
  {:len 120 :desc "Name of Port & Berth" :tag "portname"    :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len  30 :desc "Destination"          :tag "destination" :fn (partial ais-types/t ais-vocab/sixbit-ascii 5)}
  {:len  25 :desc "Longitude"            :tag "lon"         :fn common/slon}
  {:len  24 :desc "Latitude"             :tag "lat"         :fn common/slat}
  {:len  43 :desc "Spare"                :tag "spare"       :fn ais-types/x}
))
