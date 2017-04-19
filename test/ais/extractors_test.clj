(ns ais.extractors-test
  (:require [clojure.test :refer :all]
            [ais.extractors :refer :all]))


(def msg-1  "AIVDM,1,1,2,A,0ABCDEF,0*01")
(def tks-1 {
 :ty 0
 :tg nil
 :en "AIVDM,1,1,2,A,0ABCDEF,0"
 :fc 1
 :fn 1
 :pl "0ABCDEF"
 :fl 0
 :ck "01"
})

(def msg-2  "AIVDM,2,1,7,B,5ABCDEF,2*3F")
(def tks-2 {
 :ty 5
 :tg nil
 :en "AIVDM,2,1,7,B,5ABCDEF,2"
 :fc 2
 :fn 1
 :pl "5ABCDEF"
 :fl 2
 :ck "3F"
})

(def msg-3  "\\s:foobar,c:1448312100,t:1448312099*00\\AIVDM,1,1,,,:ABCDEF,3*1B")
(def tks-3 {
 :ty 10
 :tg "\\s:foobar,c:1448312100,t:1448312099*00\\"
 :en "AIVDM,1,1,,,:ABCDEF,3"
 :fc 1
 :fn 1
 :pl ":ABCDEF"
 :fl 3
 :ck "1B"
})

(def msg-4  "FOOBAR,2,1,7,B,5ABCDEF,0*3F")
(def tks-4 nil)

;; ---
;; Tests
;; ---

(deftest tokenize-test
  (testing "Parse valid message w/o tags - 1"
    (is (= (tokenize msg-1) tks-1)))
  (testing "Parse valid message w/o tags - 2"
    (is (= (tokenize msg-2) tks-2)))
  (testing "Parse message with tags"
    (is (= (tokenize msg-3) tks-3)))
  (testing "Parse invalid message"
    (is (= (tokenize msg-4) tks-4))))


(def tags "\\g:1-2-1996,s:foobar,c:1448312100,t:1448312099*00\\")

(deftest parse-test
  (testing "Parse group"
    (is (= (parse "g" tags) "2-1996")))
  (testing "Parse group"
    (is (= (parse "n" tags) nil)))
  (testing "Parse group"
    (is (= (parse "c" tags) 1448312100)))
  (testing "Parse group"
    (is (= (parse "t" tags) 1448312099)))
  (testing "Parse group"
    (is (= (parse "s" tags) "foobar")))
  (testing "Parse group"
    (is (= (parse "tags" tags) tags))))
