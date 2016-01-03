(ns ais.util-test
  (:require [clojure.test :refer :all]
            [ais.util :refer :all]))


(deftest timestamp-conversion-test
  (testing "Timestamp converted to iso format"
    (is (= (timestamp->iso 1366127520000) "20130416T115200Z")))
  (testing "Timestamp converted to yyyyMMdd format."
    (is (= (timestamp->iso 1366127520000 "yyyyMMdd") "20130416"))))

(deftest checksum-test
  (testing "Checksum matches"
    (is (= (checksum "AIVDM,1,1,,B,14eGV1P000npJ?FM14LAVVeP050h,0") "05")))
  (testing "Checksum doesn't match"
    (is (not= (checksum "AIVDM,1,1,,B,14eGV1P000npJ?FM14LAVVeP050,0") "05"))))

(deftest pad-test
  (testing "Pad input w/ 0 characters"
    (is (= (pad "11111" 0) "11111")))
  (testing "Pad input w/ 1 character"
    (is (= (pad "11111" 1) "111110")))
  (testing "Pad input w 5 characters"
    (is (= (pad "11111" 5) "1111100000"))))

(deftest bitmask-test
  (testing "Create 1 character length bit mask"
    (is (= (bitmask 1) 1)))
  (testing "Create 2 character length bit mask"
    (is (= (bitmask 2) 3)))
  (testing "Create 3 character length bit mask"
    (is (= (bitmask 3) 7))))

(deftest twos-comp-test
  (testing "-11"
    (is (= (twos-comp "11110101") -11)))
  (testing "-101"
    (is (= (twos-comp "10011011") -101)))
  (testing "-33"
    (is (= (twos-comp "11011111") -33))))

;; http://catb.org/gpsd/AIVDM.html#_aivdm_aivdo_payload_armoring
;; TODO: create table of armored values and test entire table.
(deftest char-str->decimal-test ; payload armoring test
  (testing "'0' -> 0"
    (is (= (char-str->decimal "0") 0)))
  (testing "'>' -> 14"
    (is (= (char-str->decimal ">") 14)))
  (testing "'N' -> 30"
    (is (= (char-str->decimal "N") 30))))

(deftest char->decimal-test ; payload armoring test
  (testing "\\0 -> 0"
    (is (= (char->decimal \0) 0)))
  (testing "\\i -> 49"
    (is (= (char->decimal \i) 49)))
  (testing "\\> -> 14"
    (is (= (char->decimal \>) 14)))
  (testing "\\N -> 30"
    (is (= (char->decimal \N) 30))))

(deftest decimal->binary-test
  (testing "0"
    (is (= (decimal->binary 0) "000000")))
  (testing "1"
    (is (= (decimal->binary 1) "000001")))
  (testing "32"
    (is (= (decimal->binary 32) "100000")))
  (testing "46"
    (is (= (decimal->binary 46) "101110"))))