(ns ais.types-test
  (:require [clojure.test :refer :all]
            [ais.types :refer :all]))

;; http://catb.org/gpsd/AIVDM.html#_ais_payload_data_types
(deftest u-test
  (testing "Parse unsigned int"
    (is (= (u "100000") 32))))

(deftest U-test
  (testing "Parse unsigned int w/ scale"
    (is (= (U (/ 1 2) 0 "001010") 5)))
  (testing "Parse unsigned int w/ scale - float"
    (is (= (U (/ 1 2.0) 0 "001010") 5.0)))
  (testing "Parse unsigned int w/ scale - identity factor"
    (is (= (U 1 0 "001010") 10)))) 

(deftest i-test
  (testing "Parse signed int"
    (is (= (i "11110101") -11))))

(deftest I-test
  (testing "Parse signed even int w/ scale"
    (is (= (I (/ 1 2) 0 "11110100") -6)))
  (testing "Parse signed int w/ scale - float"
    (is (= (I (/ 1 2.0) 0 "11110101") -5.5)))
  (testing "Parse signed int w/ scale - identity factor"
    (is (= (I 1 0 "11110101") -11))))

;; TODO: functions should return native boolean types, not stringified values
(deftest b-test
  (testing "Parse binary true"
    (is (= (b "1") true)))
  (testing " Parse binary false"
    (is (= (b "0") false)))
  (testing "Parse binary true - extra bits"
    (is (= (b "001") true)))
  (testing " Parse binary false -extra bits"
    (is (= (b "0000") false))))


(def e-test-vocab { 1 "foo" 2 "bar" 3 "baz" })
(deftest e-test
  (testing "Parse value / lookup value - 1"
    (is (= (e e-test-vocab "00001") "foo")))
  (testing "Parse value / lookup value - 2"
    (is (= (e e-test-vocab "00010") "bar")))
  (testing "Parse value / lookup value - 3"
    (is (= (e e-test-vocab "00011") "baz"))))

;; TODO: test sixbit string conversion