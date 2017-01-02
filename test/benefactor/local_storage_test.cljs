(ns benefactor.local-storage-test
  (:require
    [benefactor.local-storage]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest set-get-remove
  (is (nil? (benefactor.local-storage/get :key)))
  (benefactor.local-storage/set! :key "value")
  (is (= (benefactor.local-storage/get :key) "value"))
  (benefactor.local-storage/remove! :key)
  (is (nil? (benefactor.local-storage/get :key))))

(deftest set-get-clear
  (is (nil? (benefactor.local-storage/get :key)))
  (is (nil? (benefactor.local-storage/get :other-key)))
  (benefactor.local-storage/set! :key "value")
  (benefactor.local-storage/set! :other-key {:map "nested"})
  (is (= (benefactor.local-storage/get :key) "value"))
  (is (= (benefactor.local-storage/get :other-key) {:map "nested"}))
  (benefactor.local-storage/clear!)
  (is (nil? (benefactor.local-storage/get :key)))
  (is (nil? (benefactor.local-storage/get :other-key))))
