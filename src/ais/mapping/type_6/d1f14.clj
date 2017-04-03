(ns ais.mapping.type_6.d1f14
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def tidal-seq-fields (list
  {:len  27 :desc "Latitude"                :tag "lat"       :fn common/lat}
  {:len  28 :desc "Longtitude"              :tag "lon"       :fn common/lon}
  {:len   5 :desc "From UTC Hour"           :tag "from_hour" :fn ais-types/u}
  {:len   6 :desc "From UTC Minute"         :tag "from_min"  :fn ais-types/u}
  {:len   5 :desc "To UTC Hour"             :tag "to_hour"   :fn ais-types/u}
  {:len   6 :desc "To UTC Minute"           :tag "to_min"    :fn ais-types/u}
  {:len   9 :desc "Current Dir. Predicted"  :tag "cdir"      :fn ais-types/u}
  {:len   7 :desc "Current Speed Predicted" :tag "cspeed"    :fn common/speed}
))

(defn field-mapper [_]
  tidal-seq-fields)

(def bits-len (partial ais-types/array-bit-len 3 93))

(def seq-handler (partial ais-types/a 93 field-mapper common/parse-binary))

(def d1f14 (list
  {:len        4 :desc "Month"   :tag "month"   :fn ais-types/u}
  {:len        5 :desc "Day"     :tag "day"     :fn ais-types/u}
  {:len bits-len :desc "Tidal's" :tag "tidals"  :fn seq-handler :a true}
))
