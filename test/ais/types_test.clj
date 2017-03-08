(ns ais.types-test
  (:require [clojure.test :refer :all]
            [ais.core  :refer [parse-binary data-collector]]
            [ais.types :refer :all]))

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


;;;;;;;;;;;;;;;;;
;;             ;;
;; ARRAY TESTS ;;
;;             ;;
;;;;;;;;;;;;;;;;;


;;
;; Test Case 1
;;

(def simple_array-decoded {
	"three" 3,
	"four" 4,
	"array" [{
		"one" 1,
		"two" 2
	}, {
		"one" 1,
		"two" 2
	}, {
		"one" 1,
		"two" 2
	}]
})

(def simple_array_entry-fields (list
  {:len 2 :desc "One"  :tag "one" :fn u}
  {:len 2 :desc "Two"  :tag "two" :fn u}
))

(defn simple-mapper [_]
  simple_array_entry-fields)

(def simple-len (partial array-bit-len 3 4))

(def simple_array-fields (list
  {:len          3 :desc "Three" :tag "three" :fn u}
  {:len          3 :desc "Four"  :tag "four"  :fn u}
  {:len simple-len :desc "Array" :tag "array" :fn (partial a 4 simple-mapper parse-binary) :a true}
))

(def f1   "011")
(def f2   "100")
(def a_f1  "01")
(def a_f2  "10")
(def a_entry (str a_f1 a_f2))
(def simple_array-bits (str f1 f2 a_entry a_entry a_entry))


;;
;; Test Case 2
;;
(def known_size_array-decoded {
	"three" 3,
	"four"  4,
	"size"  3,
	"array" [{
		"one"   1,
		"two"   2,
		"three" 3
	}, {
		"one"   1,
		"two"   2,
		"three" 3
	}, {
		"one"   1,
		"two"   2,
		"three" 3
	}]
})

(def known_size_array_entry-fields (list
  {:len 2 :desc "One"   :tag "one"   :fn u}
  {:len 2 :desc "Two"   :tag "two"   :fn u}
  {:len 2 :desc "Three" :tag "three" :fn u}
))

(defn known_size-mapper [_]
  known_size_array_entry-fields)

;; array length function
(def known_size-len (partial array-bit-len 3 6))

(def known_size_array-fields (list
  {:len              3 :desc "Three" :tag "three" :fn u}
  {:len              3 :desc "Four"  :tag "four"  :fn u}
  {:len              2 :desc "Size"  :tag "size"  :fn u}
  {:len known_size-len :desc "Array" :tag "array" :fn (partial a 6 known_size-mapper parse-binary) :a true}
))

(def f1  "011")
(def f2  "100")
(def f3   "11")
(def a_f1 "01")
(def a_f2 "10")
(def a_f3 "11")
(def a_entry (str a_f1 a_f2 a_f3))
(def known_size_array-bits (str f1 f2 f3 a_entry a_entry a_entry))


;;
;; Test Case 3
;;
(def dynamic_mapped_array-decoded {
	"three" 3,
	"four"  4,
	"array" [{
		"three-1" 3,
		"three-2" 3,
		"three-3" 3
	}, {
		"two-1" 2,
		"two-2" 2,
		"two-3" 2
	}, {
		"one-1" 1,
		"one-2" 1,
		"one-3" 1
	}]
})

(def foo (list
  {:len 2 :desc "One" :tag "one-1" :fn u}
  {:len 2 :desc "One" :tag "one-2" :fn u}
  {:len 2 :desc "One" :tag "one-3" :fn u}
))

(def bar (list
  {:len 2 :desc "Two" :tag "two-1" :fn u}
  {:len 2 :desc "Two" :tag "two-2" :fn u}
  {:len 2 :desc "Two" :tag "two-3" :fn u}
))

(def baz (list
  {:len 2 :desc "Three" :tag "three-1" :fn u}
  {:len 2 :desc "Three" :tag "three-2" :fn u}
  {:len 2 :desc "Three" :tag "three-3" :fn u}
))

(defn dynamic_array-mapper [bits]
  (let [map-id (u (subs bits 0 2))]
    (condp = map-id
      1 foo
      2 bar
      3 baz
      (throw (java.lang.Exception. (format "Unknown map-id: %s" map-id))))))

(def dynamic_array-len (partial array-bit-len 3 6))

(def dynamic_array-fields (list
  {:len          3 :desc "Three" :tag "three" :fn u}
  {:len          3 :desc "Four"  :tag "four"  :fn u}
  {:len dynamic_array-len :desc "Array" :tag "array" :fn (partial a 6 dynamic_array-mapper parse-binary) :a true}
))

(def f1 "011")
(def f2 "100")
(def a_foo "010101")
(def a_bar "101010")
(def a_baz "111111")
(def dynamic_mapped_array-bits (str f1 f2 a_baz a_bar a_foo))


;;
;; Test Case 4
;;
(def embedded_array-decoded {
	"three" 3,
	"four"  4,
	"array" [{
		"three-1" 3,
		"three-2" 3,
		"array" [{
			"one" 1,
			"two" 2
		}, {
			"one" 1,
			"two" 2
		}, {
			"one" 1,
			"two" 2
		}]
	}, {
		"two-1" 2,
		"two-2" 2,
		"array" [{
			"one" 1,
			"two" 2
		}, {
			"one" 1,
			"two" 2
		}, {
			"one" 1,
			"two" 2
		}]
	}, {
		"one-1" 1,
		"one-2" 1,
		"array" [{
			"one" 1,
			"two" 2
		}, {
			"one" 1,
			"two" 2
		}, {
			"one" 1,
			"two" 2
		}]
	}]
})

(def sub-array-max-size 3)
(def sub-array-element-size 4)

;; len is known at compile time
(defn sub_array-len [& _]
  (* sub-array-max-size sub-array-element-size))

(defn sub_array-mapper [_]
  (list
    {:len 2 :desc "One"  :tag "one" :fn u}
    {:len 2 :desc "Two"  :tag "two" :fn u}))

(def sub_array-fields
  {:len  sub_array-len
   :desc "Array"
   :tag  "array"
   :fn   (partial a 4 sub_array-mapper parse-binary)
   :a    true})

(def oof (list
  {:len 2 :desc "One" :tag "one-1" :fn u}
  {:len 2 :desc "One" :tag "one-2" :fn u}
  sub_array-fields
))

(def rab (list
  {:len 2 :desc "Two" :tag "two-1" :fn u}
  {:len 2 :desc "Two" :tag "two-2" :fn u}
  sub_array-fields
))

(def zab (list
  {:len 2 :desc "Three" :tag "three-1" :fn u}
  {:len 2 :desc "Three" :tag "three-2" :fn u}
  sub_array-fields
))

(defn embedded_array-mapper [bits]
  (let [map-id (u (subs bits 0 2))]
    ;(println (format "map-id=%d" map-id))
    (condp = map-id
      1 oof
      2 rab
      3 zab
      (throw (java.lang.Exception. (format "Unknown map-id: %s" map-id))))))

(def embedded_array-len (partial array-bit-len 3 16))

(def embedded_array-fields (list
  {:len         3 :desc "Three" :tag "three" :fn u}
  {:len         3 :desc "Four"  :tag "four"  :fn u}
  {:len embedded_array-len :desc "Array" :tag "array" :fn (partial a 16 embedded_array-mapper parse-binary) :a true}
))

(def f1 "011")
(def f2 "100")
(def sub_a "0110")
(def a_oof (str "0101" sub_a sub_a sub_a))
(def a_rab (str "1010" sub_a sub_a sub_a))
(def a_zab (str "1111" sub_a sub_a sub_a))
(def embedded_array-bits (str f1 f2 a_zab a_rab a_oof))



;;
;; Test Cases
;;

(deftest a-test
  (let [[acc collector] (data-collector "json")]
    (testing "Parse binary array with simple fields"
      (let [actual (parse-binary simple_array-fields acc collector simple_array-bits)]
        (is (= actual simple_array-decoded))))

    (testing "Parse binary array w/s size determined by prior field"
      (let [actual (parse-binary known_size_array-fields acc collector known_size_array-bits)]
        (is (= actual known_size_array-decoded))))

    (testing "Parse binary array with elements determined from dynamic mapper"
      (let [actual (parse-binary dynamic_array-fields acc collector dynamic_mapped_array-bits)]
        (is (= actual dynamic_mapped_array-decoded))))

    (testing "Parse binary array with array elements"
      (let [actual (parse-binary embedded_array-fields acc collector embedded_array-bits)]
        (is (= actual embedded_array-decoded))))))
