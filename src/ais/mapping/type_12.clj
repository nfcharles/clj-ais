(ns ais.mapping.type_12
  (:require [ais.types :as ais-types]
            [ais.vocab :as ais-vocab]
            [ais.mapping.common :as common])
  (:gen-class))


(def mapping-12 (list
  {:len        6 :desc "Message Type"     :tag "type"       :fn (partial common/const 12)}
  {:len        2 :desc "Repeat Indicator" :tag "repeat"     :fn ais-types/u}
  {:len       30 :desc "Source MMSI"      :tag "mmsi"       :fn ais-types/u}
  {:len        2 :desc "Sequence Number"  :tag "seqno"      :fn ais-types/u}
  {:len       30 :desc "Destination MMSI" :tag "dest_mmsi"  :fn ais-types/u}
  {:len        1 :desc "Retransmit flag"  :tag "retransmit" :fn ais-types/b}
  {:len        1 :desc "Spare"            :tag "spare"      :fn ais-types/x}
  {:len      936 :desc "Text"             :tag "text"       :fn (partial ais-types/t ais-vocab/sixbit-ascii 26)}
))
