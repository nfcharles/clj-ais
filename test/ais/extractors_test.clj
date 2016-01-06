(ns ais.extractors-test
  (:require [clojure.test :refer :all]
            [ais.extractors :refer :all]))


(def aivdm-message "\\c:1448312100,t:1448312099*00\\!AIVDM,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")
(def aivdo-message "\\c:1448312100,t:1448312099*00\\!AIVDO,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")
(def group-message (list "\\g:1-2-1996,c:1446625797*57\\!AIVDM,2,1,6,A,581:K8@2<lS5KL@;V20dTpN0E8T>22222222221@BPR=>6e50HT2DQ@EDlp8,0*19" "\\g:2-2-1996*5A\\!AIVDM,2,2,6,A,88888888880,2*22"))

(deftest extract-packet-type-test
  (testing "Extract packet type from AIVDM sentence"
    (is (= (extract-packet-type aivdm-message) "AIVDM")))
  (testing "Extract packet type from AIVDO sentence"
    (is (= (extract-packet-type aivdo-message) "AIVDO"))))

(deftest extract-timestamp-test
  (testing "Extract tag block timestamp"
    (is (= (extract-timestamp aivdm-message) "1448312100"))))

(deftest extract-group-test
  (testing "Extract group from message"
    (is (= (extract-group (nth group-message 0)) "2-1996A"))))

(deftest extract-tag-block-test
  (testing "Extract tag block - 1"
    (is (= (extract-tag-block aivdm-message) "c:1448312100,t:1448312099*00"))))