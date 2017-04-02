(ns ais.mapping.type_6.f1d16
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def f1d16 (list
  {:len  14 :desc "# persons on board" :tag "persons" :fn ais-types/u}
  {:len   3 :desc "Spare"              :tag "spare"   :fn ais-types/x}
))
