(ns benefactor.cookies-test
  (:require
    [benefactor.cookies]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest set-get-remove
  (is (nil? (benefactor.cookies/get :key)))
  (benefactor.cookies/set! :key "value")
  (is (= (benefactor.cookies/get :key) "value"))
  (benefactor.cookies/remove! :key)
  (is (nil? (benefactor.cookies/get :key))))

(deftest set-get-clear
  (is (nil? (benefactor.cookies/get :key)))
  (is (nil? (benefactor.cookies/get :other-key)))
  (benefactor.cookies/set! :key "value")
  (benefactor.cookies/set! :other-key {:map "nested"})
  (is (= (benefactor.cookies/get :key) "value"))
  (is (= (benefactor.cookies/get :other-key) {:map "nested"}))
  (benefactor.cookies/clear!)
  (is (nil? (benefactor.cookies/get :key)))
  (is (nil? (benefactor.cookies/get :other-key))))
