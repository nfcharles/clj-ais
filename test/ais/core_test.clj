(ns ais.core-test
  (:require [clojure.test :refer :all]
            [ais.core :refer :all]))

(def aivdm-message "\\c:1448312100,t:1448312099*00\\!AIVDM,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")
(def aivdm-message-no-time "!AIVDM,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")


(deftest parse-tag-block-test
  (testing "Parse tag block"
    (is (= (parse-tag-block aivdm-message) (hash-map "timestamp" "20151123T155500Z"))))
  (testing "Parse tag block - no stamp"
    (is (= (parse-tag-block aivdm-message-no-time) (hash-map "timestamp" nil)))))