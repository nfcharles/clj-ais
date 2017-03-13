(ns ais.mapping.core
  (:require [clojure.string :as string])
  (:require [ais.vocab :as ais-vocab])
  (:require [ais.types :as ais-types])
  (:require [ais.util :as ais-util])
  (:require [ais.extractors :as ais-ex])
  (:require [ais.mapping.common :as ais-map-comm])
  (:require [ais.mapping.type_1_2_3 :as type_1_2_3])
  (:require [ais.mapping.type_4  :as type_4])
  (:require [ais.mapping.type_5  :as type_5])
  (:require [ais.mapping.type_6  :as type_6])
  (:require [ais.mapping.type_7  :as type_7])
  (:require [ais.mapping.type_9  :as type_9])
  (:require [ais.mapping.type_10 :as type_10])
  (:require [ais.mapping.type_12 :as type_12])
  (:require [ais.mapping.type_14 :as type_14])
  (:require [ais.mapping.type_18 :as type_18])
  (:require [ais.mapping.type_19 :as type_19])
  (:require [ais.mapping.type_20 :as type_20])
  (:require [ais.mapping.type_21 :as type_21])
  (:require [ais.mapping.type_24 :as type_24])  
  (:gen-class))


(def parse-binary ais-map-comm/parse-binary)

(def tag-mapping (hash-map
  "c" { :desc  "Timestamp" 
        :tag   "timestamp" 
        :ex-fn (partial ais-ex/parse "c")
        :fn    #(ais-util/timestamp->iso (* 1000 %)) } 
  "s" { :desc  "Source"
        :tag   "station"
        :ex-fn (partial ais-ex/parse "s")
        :fn    #(identity %)} 
  "n" { :desc  "Line"
        :tag   "line"
        :ex-fn (partial ais-ex/parse "n")
        :fn    #(read-string %) } ))

(defn- get-type [bits]
  (ais-types/u nil (subs bits 0 6)))

(defmulti parsing-rules 
  (fn [bits] (get-type bits)))

(defmethod parsing-rules  1 [bits] type_1_2_3/mapping-1)
(defmethod parsing-rules  2 [bits] type_1_2_3/mapping-2)
(defmethod parsing-rules  3 [bits] type_1_2_3/mapping-3)
(defmethod parsing-rules  4 [bits] type_4/mapping-4)
(defmethod parsing-rules  5 [bits] type_5/mapping-5)
(defmethod parsing-rules  6 [bits] (type_6/determine-6-map bits))
(defmethod parsing-rules  7 [bits] type_7/mapping-7)
(defmethod parsing-rules  9 [bits] type_9/mapping-9)
(defmethod parsing-rules 10 [bits] type_10/mapping-10)
(defmethod parsing-rules 12 [bits] type_12/mapping-12)
(defmethod parsing-rules 14 [bits] type_14/mapping-14)
(defmethod parsing-rules 18 [bits] type_18/mapping-18)
(defmethod parsing-rules 19 [bits] type_19/mapping-19)
(defmethod parsing-rules 20 [bits] type_20/mapping-20)
(defmethod parsing-rules 21 [bits] type_21/mapping-21)
(defmethod parsing-rules 24 [bits] (type_24/determine-24-map bits))
