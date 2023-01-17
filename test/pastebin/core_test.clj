(ns pastebin.core-test
  (:require [clojure.test :refer :all]
            [pastebin.core :refer :all]))

(deftest test-md5
  (testing "md5 test"
    (is (= (md5sum "a") "0cc175b9c0f1b6a831c399e269772661"))))

(deftest test-add-item
  (testing "add-item key-and-value"
    (let [db (atom {}) data "some data"]
      (is (= (add-item db "123" data) {:key "123"}))))
  (testing "add-item value only "
    (let [db (atom {}) data "a"]
      (is (= (add-item db data) {:key "0cc175b9c0f1b6a831c399e269772661"})))))

(deftest test-get-item
  (testing "get item when exists"
    (let [db (atom {"1234" "ABCD"}) id "1234"]
      (is (= (get-item db id) {:paste "ABCD"}))))
  (testing "get-item when doestn exist"
    (let [db (atom {}) id "1234"]
      (is (= (get-item db id) {:paste nil})))))
