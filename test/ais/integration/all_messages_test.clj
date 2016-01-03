(ns ais.integration.all_messages-test
  (:require [clojure.test :refer :all]
            [ais.core :refer :all]
            [clojure.data.json :as json]))

; http://fossies.org/linux/gpsd/test/sample.aivdm

(def type-1 {
  "accuracy" "True"
  "course"   224
  "heading"  215
  "lat"      48.38163333333
  "lon"      -123.395383333
  "maneuver" "Not available (default)"
  "mmsi"     371798000
  "radio"    0
  "raim"     "False"
  "repeat"   0
  "second"   33
  "spare"    "000"
  "speed"    12.3
  "status"   0
  "turn"     -127
})

;(deftest type-1-test
;  (testing "Decode type 1"
;    (is (= (parse "!AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0*4A") type-1))))