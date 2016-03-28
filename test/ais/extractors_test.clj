(ns ais.extractors-test
  (:require [clojure.test :refer :all]
            [ais.extractors :refer :all]))


(def aivdm-message "\\c:1448312100,t:1448312099*00\\!AIVDM,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")
(def aivdo-message "\\c:1448312100,s:FooBar,n:12345*00\\!AIVDO,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")
(def tricky-message "\\c:1448312100,t:1448341521*00\\!AIVDM,1,1,,B,13n:Tq0000PJ3QpQivJ>42An26SH,0*52")
(def group-message (list "\\g:1-2-1996,c:1446625797*57\\!AIVDM,2,1,6,A,581:K8@2<lS5KL@;V20dTpN0E8T>22222222221@BPR=>6e50HT2DQ@EDlp8,0*19" "\\g:2-2-1996*5A\\!AIVDM,2,2,6,A,88888888880,2*22"))


(def case-1 (hash-map
  "c"          1448312100
  "t"          1448312099
  "tags"       "\\c:1448312100,t:1448312099*00\\"
  "pac-type"   "AIVDM"
  "frag-count" 1
  "frag-num"   1
  "frag-info"  [1 1]
  "seq-id"     ""
  "radio-ch"   "B"
  "payload"    "35NVMmg00026=kRGFbD=4a;N0UFC"
  "fill-bits"  0
  "checksum"   "16"
  "env-chksum" ["AIVDM,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0", "16"] ))

(def case-2 (hash-map
  "c"          1448312100
  "s"          "FooBar"
  "n"          12345
  "tags"       "\\c:1448312100,s:FooBar,n:12345*00\\"
  "pac-type"   "AIVDO"
  "frag-count" 1
  "frag-num"   1
  "frag-info"  [1 1]
  "seq-id"     ""
  "radio-ch"   "B"
  "payload"    "35NVMmg00026=kRGFbD=4a;N0UFC"
  "fill-bits"  0
  "checksum"   "16"
  "env-chksum" ["AIVDO,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0" "16"] ))

(deftest extractor-test
  (testing "Case 1"
    (doseq [[field value] case-1 ]
      (is (= (parse field aivdm-message) value))))
  (testing "Case 2"
    (doseq [[field value] case-2 ]
      (is (= (parse field aivdo-message) value)))))

(deftest extract-group-test
  (testing "Group extraction"
    (is (= (parse "g" (nth group-message 0)) "2-1996"))
    (is (= (parse "g" (nth group-message 1)) "2-1996"))))

(deftest extract-tags-test
  (testing "Tag extraction"
    (is (= (parse "n" tricky-message) nil))
    (is (= (parse "c" tricky-message) 1448312100))
    (is (= (parse "t" tricky-message) 1448341521))))
