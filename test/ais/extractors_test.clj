(ns ais.extractors-test
  (:require [clojure.test :refer :all]
            [ais.extractors :refer :all]))


(def test-aivdm-envelope "!AIVDM,2,2,1,B,PH8888888888880,2*3E")
(def test-aivdo-envelope "!AIVDO,2,2,1,B,PH8888888888880,2*3E")


(deftest extract-packet-type-test
  (testing "Extract packet type from AIVDM sentence"
    (is (= (extract-packet-type test-aivdm-envelope) "AIVDM")))
  (testing "Extract packet type from AIVDO sentence"
    (is (= (extract-packet-type test-aivdo-envelope) "AIVDO"))))

;(deftest extract-group-test
;  (testing "Extract group from message"
;    (is (= (extract-group test-message) "2-1996A"))))

