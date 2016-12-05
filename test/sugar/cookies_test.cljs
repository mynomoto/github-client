(ns sugar.cookies-test
  (:require
    [sugar.cookies]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest set-get-remove
  (is (nil? (sugar.cookies/get :key)))
  (sugar.cookies/set! :key "value")
  (is (= (sugar.cookies/get :key) "value"))
  (sugar.cookies/remove! :key)
  (is (nil? (sugar.cookies/get :key))))

(deftest set-get-clear
  (is (nil? (sugar.cookies/get :key)))
  (is (nil? (sugar.cookies/get :other-key)))
  (sugar.cookies/set! :key "value")
  (sugar.cookies/set! :other-key {:map "nested"})
  (is (= (sugar.cookies/get :key) "value"))
  (is (= (sugar.cookies/get :other-key) {:map "nested"}))
  (sugar.cookies/clear!)
  (is (nil? (sugar.cookies/get :key)))
  (is (nil? (sugar.cookies/get :other-key))))
