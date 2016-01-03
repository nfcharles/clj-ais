(ns ais.extractors-test
  (:require [clojure.test :refer :all]
            [ais.extractors :refer :all]))


(def test-message "\\g:1-2-1996,s:rORBCOMM000,c:1446625797*57\\!AIVDM,2,1,6,A,581:K8@2<lS5KL@;V20dTpN0E8T>22222222221@BPR=>6e50HT2DQ@EDlp8,0*19")
(def test-aivdm-envelope "!AIVDM,2,2,1,B,PH8888888888880,2*3E")
(def test-aivdo-envelope "!AIVDO,2,2,1,B,PH8888888888880,2*3E")


(deftest extract-packet-type-test
  (testing "Extract packet type from AIVDM sentence"
    (is (= (extract-packet-type test-aivdm-envelope) "AIVDM")))
  (testing "Extract packet type from AIVDO sentence"
    (is (= (extract-packet-type test-aivdo-envelope) "AIVDO"))))

(deftest extract-group-test
  (testing "Extract group from message"
    (is (= (extract-group test-message) "2-1996A"))))

