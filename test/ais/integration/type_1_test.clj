(ns ais.integration.type_1-test
  (:require [clojure.test :refer :all]
            [ais.core :refer :all]))

;;;; Integration test source messages
; http://fossies.org/linux/gpsd/test/sample.aivdm

;   "radio"    #(identity %) 
;   "status"   #(identity %) ; source doesn't map integer value to dictionary lookup, exclude


(def fields
 (hash-map 
   "timestamp" #(identity %)
   "accuracy"  #(identity %)
   "course"    #(float %)
   "heading"   #(identity %)
   "lat"       #(format "%.2f" %)
   "lon"       #(format "%.2f" %)
   "mmsi"      #(identity %)
   "raim"      #(identity %)
   "repeat"    #(identity %)
   "second"    #(identity %)
   "speed"     #(identity %)
   "turn"      #(identity %)))

(defn- select-fields [x]
  (into {} (for [[k v] fields] [k (v (x k))])))

(def type-1-1 
  (hash-map
    "timestamp" nil
    "accuracy" true
    "course"   224
    "heading"  215
    "lat"      48.38163333333
    "lon"      -123.395383333
    "maneuver" "Not available (default)"
    "mmsi"     371798000
    "radio"    0
    "raim"     false
    "repeat"   0
    "second"   33
    "spare"    "000"
    "speed"    12.3
    "status"   0
    "turn"     -720 ))

(def type-1-2
  (hash-map
    "timestamp" nil
    "accuracy" false
    "course"   93.4
    "heading"  511
    "lat"      43.08015
    "lon"      -70.7582
    "maneuver" "Not available (default)"
    "mmsi"     440348000
    "radio"    0
    "raim"     false
    "repeat"   0
    "second"   13
    "spare"    "000"
    "speed"    0.0
    "status"   0
    "turn"     -731 ))

;; TODO: Test csv

(deftest type-1-test
  (testing "Type 1 decode - 1"
    (let [expected (select-fields type-1-1)
    	  actual (select-fields (parse "json" "!AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0*4A"))]
      (is (= expected actual))))
  (testing "Type 1 decode - 2"
    (let [expected (select-fields type-1-2)
    	  actual (select-fields (parse "json" "!AIVDM,1,1,,A,16SteH0P00Jt63hHaa6SagvJ087r,0*42"))]
      (is (= expected actual)))))
