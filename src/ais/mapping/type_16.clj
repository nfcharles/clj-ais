(ns ais.mapping.type_16
  (:require [ais.types :as ais-types]
            [ais.util :as ais-util]
            [ais.mapping.common :as common])
  (:gen-class))


;; TODO: This configuration fails during live testing.  Type 16 messages tested were 168 length bit strings.
;; Either spec is defined incorrectly/outdated or messages are corrupted.  Will investigate further later.

(def mmsi-seq-fields (list
  {:len 30 :desc "Destination MMSI" :tag "mmsi"      :fn ais-types/u}
  {:len 12 :desc "Offset"           :tag "offset"    :fn ais-types/u}
  {:len 10 :desc "Increment"        :tag "increment" :fn ais-types/u}
))

(defn field-mapper [_]
  mmsi-seq-fields)

(def bits-len (partial ais-types/array-bit-len 2 52))

(def seq-handler (partial ais-types/a 52 field-mapper ais-util/parse-binary))

(def mapping-16-base  (list
  {:len  6 :desc "Message Type"     :tag "type"   :fn (partial common/const 16)}
  {:len  2 :desc "Repeat Indicator" :tag "repeat" :fn ais-types/u}
  {:len 30 :desc "Source MMSI"      :tag "mmsi"   :fn ais-types/u}
  {:len  2 :desc "Spare"            :tag "spare"  :fn ais-types/x}
))

(def mapping-16-96 (list
  (concat
    mapping-16-base
    mmsi-seq-fields
    (list {:len 4 :desc "Spare" :tag "spare" :fn ais-types/x}))))

(def mapping-16
  (concat
    mapping-16-base
    (list {:len bits-len :desc "MMSIs" :tag "mmsis" :fn seq-handler :a true})))

(defn determine-16-map [bits]
  (let [n (count bits)]
    (case n
       96 mapping-16-96
      144 mapping-16
      (throw (java.lang.Exception. (format "Unexpected bit length for type 16: %s" n))))))
