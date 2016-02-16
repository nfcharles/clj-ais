(ns ais.core-test
  (:require [clojure.test :refer :all]
            [ais.core :refer :all]))

(def aivdm-message "\\s:FooBar,c:1448312100,t:1448312099*00\\!AIVDM,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")
(def aivdm-message-no-tags "!AIVDM,1,1,,B,35NVMmg00026=kRGFbD=4a;N0UFC,0*16")
(def group-message (list "\\g:1-2-1996,c:1446625797*57\\!AIVDM,2,1,6,A,581:K8@2<lS5KL@;V20dTpN0E8T>22222222221@BPR=>6e50HT2DQ@EDlp8,0*19" "\\g:2-2-1996*5A\\!AIVDM,2,2,6,A,88888888880,2*22"))

(def assembled-group "\\g:1-2-1996,c:1446625797*57\\!AIVDM,1,1,1,A,581:K8@2<lS5KL@;V20dTpN0E8T>22222222221@BPR=>6e50HT2DQ@EDlp888888888880,2*2F")

(defn- parse-field [bits]
  (Integer/parseInt bits 2))

;; ---
;; Payload 1
;; ---

(def map-1 (list
  {:len 2 :desc "First"  :tag "first"  :fn parse-field}
  {:len 4 :desc "Second" :tag "second" :fn parse-field}
  {:len 6 :desc "Third"  :tag "third"  :fn parse-field} ))

(def bin-load-1 "010010000011")

(def dec-load-1 {"first" 1 "second" 2 "third" 3})

;; ---
;; Payload 2
;; ---
(def map-2 (list
  {:len 6 :desc "First"  :tag "first"  :fn parse-field}
  {:len 6 :desc "Second" :tag "second" :fn parse-field}
  {:len 6 :desc "Third"  :tag "third"  :fn parse-field} ))

(def bin-load-2 "000011001100110000")

(def dec-load-2 [3 12 48])

;; ---
;; Tests
;; ---

(deftest data-collector-test 
  (testing "Output type - json"
    (let [[acc collector] (data-collector "json")]
      (is (= acc {}))
      (is (= (hash-map :foo "bar") (collector acc :foo "bar")))))
  (testing "Output type - csv"
    (let [[acc collector] (data-collector "csv")]
      (is (= acc []))
      (is (= (list "bar") (collector acc :foo "bar"))))))

(deftest parse-tag-block-test
  (testing "Parse 'c' tag - json"
    (let [[acc collector] (data-collector "json")]
      (is (= (parse-tag-block acc collector aivdm-message ["c"]) {"timestamp" "20151123T155500Z"}))))
  (testing "Parse 'c' tag - csv"
    (let [[acc collector] (data-collector "csv")]
      (is (= (parse-tag-block acc collector aivdm-message ["c"]) ["20151123T155500Z"]))))
  (testing "Parse 'c' tag from tagless message"
    (let [[acc collector] (data-collector "json")]
      (is (= (parse-tag-block acc collector aivdm-message-no-tags ["c"]) {"timestamp" nil}))))
  (testing "Parse 'c' tag from tagless message"
    (let [[acc collector] (data-collector "csv")]
      (is (= (parse-tag-block acc collector aivdm-message-no-tags ["c"]) [nil]))))
  (testing "Parse 's' tag - json"
    (let [[acc collector] (data-collector "json")]
      (is (= (parse-tag-block acc collector aivdm-message ["s"]) {"station" "FooBar"}))))
  (testing "Parse 's' tag from tagless message"
    (let [[acc collector] (data-collector "json")]
      (is (= (parse-tag-block acc collector aivdm-message-no-tags ["s"]) {"station" nil}))))
  (testing "Parse multiple tags - json"
    (let [[acc collector] (data-collector "json")]
      (is (= (parse-tag-block acc collector aivdm-message ["c" "s"]) {"timestamp" "20151123T155500Z" "station" "FooBar"})))))

(deftest decode-binary-payload-test
  (testing "Decode binary payload - json"
    (let [[acc collector] (data-collector "json")]
      (is (= (decode-binary-payload map-1 acc collector bin-load-1) dec-load-1))))
  (testing "Decode binary payload - csv"
    (let [[acc collector] (data-collector "csv")]
      (is (= (decode-binary-payload map-2 acc collector bin-load-2) dec-load-2)))))

(deftest parse-group-test
  (testing "Coalesce mutipart message"
    (is (= (coalesce-group group-message) assembled-group))))
