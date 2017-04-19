(ns ais.core-test
  (:require [clojure.test :refer :all]
            [ais.core :refer :all]))


;; ---
;; Tests
;; ---

(deftest payload-test
  (testing "Single fragment payload"
    (is (= (payload 1 [{:pl "foo"}]) "foo")))
  (testing "Multiple fragment payload - 1"
    (is (= (payload 2 [{:pl "foo"} {:pl "bar"}]) "foobar")))
  (testing "Multiple fragment payload - 2"
    (is (= (payload 3 [{:pl "foo"} {:pl "bar"} {:pl "baz"}]) "foobarbaz"))))
