(ns pastebin.filestore-test
  (:require [clojure.test :refer :all]
            [pastebin.filestore :refer :all]))

(deftest test-put-in-memstore
  (testing "key in store"
    (is (= (put-in-store (atom {}) "123 MTIzNDU=") {:key "123"})))
  (testing "value in store"
    (let [db (atom {})
          k (put-in-store db "123 MTIzNDU=")]
      (is (= (@db "123") "12345")))))
