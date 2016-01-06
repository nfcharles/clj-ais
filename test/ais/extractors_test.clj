(ns ais.extractors-test
  (:require [clojure.test :refer :all]
            [ais.extractors :refer :all]))


(def test-message "\\g:1-2-73874,n:157036,s:r003669945,c:1241544035*4A\\!AIVDM,1,1,,B,15N4cJ`005Jrek0H@9n`DW5608EP,0*13")
(def test-aivdm-envelope "!AIVDM,2,2,1,B,PH8888888888880,2*3E")
(def test-aivdo-envelope "!AIVDO,2,2,1,B,PH8888888888880,2*3E")


(deftest extract-packet-type-test
  (testing "Extract packet type from AIVDM sentence"
    (is (= (extract-packet-type test-aivdm-envelope) "AIVDM")))
  (testing "Extract packet type from AIVDO sentence"
    (is (= (extract-packet-type test-aivdo-envelope) "AIVDO"))))

(deftest extract-timestamp-test
  (testing "Extract tag block timestamp"
    (is (= (extract-timestamp test-message) "1241544035"))))

(deftest extract-group-test
  (testing "Extract group from message"
    (is (= (extract-group test-message) "2-73874B"))))

