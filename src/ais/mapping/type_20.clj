(ns ais.mapping.type_20
  (:require [clojure.string :as string])
  (:require [ais.vocab :as ais-vocab])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mapping.common :as ais-map-comm])
  (:gen-class))


(def mapping-20 (list
  {:len   6 :desc "Message Type"      :tag "type"         :fn (partial ais-map-comm/const 20)}
  {:len   2 :desc "Repeat Indicator"  :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"              :tag "mmsi"         :fn ais-types/u}
  {:len   2 :desc "Spare"             :tag "spare"        :fn ais-types/x}
  {:len  12 :desc "Offset number 1"   :tag "offset1"      :fn ais-types/u}
  {:len   4 :desc "Reserved slots"    :tag "number1"      :fn ais-types/u}
  {:len   3 :desc "Time-out"          :tag "timeout1"     :fn ais-types/u}
  {:len  11 :desc "Increment"         :tag "increment1"   :fn ais-types/u}
  {:len  12 :desc "Offset number 2"   :tag "offset2"      :fn ais-types/u}
  {:len   4 :desc "Reserved slots"    :tag "number2"      :fn ais-types/u}
  {:len   3 :desc "Time-out"          :tag "timeout2"     :fn ais-types/u}
  {:len  11 :desc "Increment"         :tag "increment2"   :fn ais-types/u}
  {:len  12 :desc "Offset number 3"   :tag "offset3"      :fn ais-types/u}
  {:len   4 :desc "Reserved slots"    :tag "number3"      :fn ais-types/u}
  {:len   3 :desc "Time-out"          :tag "timeout3"     :fn ais-types/u}
  {:len  11 :desc "Increment"         :tag "increment3"   :fn ais-types/u}
  {:len  12 :desc "Offset number 4"   :tag "offset4"      :fn ais-types/u}
  {:len   4 :desc "Reserved slots"    :tag "number4"      :fn ais-types/u}
  {:len   3 :desc "Time-out"          :tag "timeout4"     :fn ais-types/u}
  {:len  11 :desc "Increment"         :tag "increment4"   :fn ais-types/u}
))

