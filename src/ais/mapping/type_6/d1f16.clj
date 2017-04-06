(ns ais.mapping.type_6.d1f16
  (:require [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))

(def d1f16 (list
  {:len  14 :desc "# persons on board" :tag "persons" :fn ais-types/u}
  {:len   3 :desc "Spare"              :tag "spare"   :fn ais-types/x}
))
