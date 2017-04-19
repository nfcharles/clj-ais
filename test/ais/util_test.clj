(ns ais.util-test
  (:require [clojure.test :refer :all]
            [ais.util :refer :all]))


(deftest timestamp-conversion-test
  (testing "Timestamp converted to iso format"
    (is (= (timestamp->iso 1366127520000) "20130416155200")))
  (testing "Timestamp converted to yyyyMMdd format."
    (is (= (timestamp->iso 1366127520000 "yyyyMMdd") "20130416"))))

(deftest checksum-test
  (testing "Checksum matches"
    (is (= (checksum "AIVDM,1,1,,B,14eGV1P000npJ?FM14LAVVeP050h,0") "05")))
  (testing "Checksum doesn't match"
    (is (not= (checksum "AIVDM,1,1,,B,14eGV1P000npJ?FM14LAVVeP050,0") "05"))))

(deftest twos-comp-test
  (testing "-11"
    (is (= (twos-comp "11110101") -11)))
  (testing "-101"
    (is (= (twos-comp "10011011") -101)))
  (testing "-33"
    (is (= (twos-comp "11011111") -33))))
