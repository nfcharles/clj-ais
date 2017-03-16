(ns ais.mapping.core
  (:require [clojure.string :as string]
            [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.util :as ais-util]
            [ais.extractors :as ais-ex]
            [ais.mapping.common :as ais-map-comm]
            [ais.mapping.type_1_2_3 :as type_1_2_3]
            [ais.mapping.type_4  :as type_4]
            [ais.mapping.type_5  :as type_5]
            [ais.mapping.type_6  :as type_6]
            [ais.mapping.type_7  :as type_7]
            [ais.mapping.type_9  :as type_9]
            [ais.mapping.type_10 :as type_10]
            [ais.mapping.type_12 :as type_12]
            [ais.mapping.type_14 :as type_14]
            [ais.mapping.type_18 :as type_18]
            [ais.mapping.type_19 :as type_19]
            [ais.mapping.type_20 :as type_20]
            [ais.mapping.type_21 :as type_21]
            [ais.mapping.type_24 :as type_24])
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


(defn parse-tag-block [acc collector tags block]
  "Decodes ais message tag block"
  (loop [t tags
         a acc]
    (if-let [tag (first t)]
      (let [fld (tag-mapping tag)
            value ((fld :ex-fn) block)]
        (recur (rest t)
               (collector a (fld :tag) (if (nil? value) nil ((fld :fn) value)))))
      a)))

(defn- get-type [bits]
  (ais-types/u nil (subs bits 0 6)))

(defn parsing-rules [bits]
  (let [msg-type (get-type bits)]
    (case (int msg-type)
      1 type_1_2_3/mapping-1
      2 type_1_2_3/mapping-2
      3 type_1_2_3/mapping-3
      4 type_4/mapping-4
      5 type_5/mapping-5
      6 (type_6/determine-6-map bits)
      7 type_7/mapping-7
      9 type_9/mapping-9
     10 type_10/mapping-10
     12 type_12/mapping-12
     14 type_14/mapping-14
     18 type_18/mapping-18
     19 type_19/mapping-19
     20 type_20/mapping-20
     21 type_21/mapping-21
     24 (type_24/determine-24-map bits)
     (throw (java.lang.Exception. (format "No configuration found for type %s" msg-type))))))
