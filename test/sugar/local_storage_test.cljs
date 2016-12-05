(ns sugar.local-storage-test
  (:require
    [sugar.local-storage]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest set-get-remove
  (is (nil? (sugar.local-storage/get :key)))
  (sugar.local-storage/set! :key "value")
  (is (= (sugar.local-storage/get :key) "value"))
  (sugar.local-storage/remove! :key)
  (is (nil? (sugar.local-storage/get :key))))

(deftest set-get-clear
  (is (nil? (sugar.local-storage/get :key)))
  (is (nil? (sugar.local-storage/get :other-key)))
  (sugar.local-storage/set! :key "value")
  (sugar.local-storage/set! :other-key {:map "nested"})
  (is (= (sugar.local-storage/get :key) "value"))
  (is (= (sugar.local-storage/get :other-key) {:map "nested"}))
  (sugar.local-storage/clear!)
  (is (nil? (sugar.local-storage/get :key)))
  (is (nil? (sugar.local-storage/get :other-key))))
