(ns benefactor.transit-test
  (:require
    [benefactor.transit]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest edn->transit->edn
  (is (= {:a "value-a"}
    (-> {:a "value-a"}
        benefactor.transit/serialize
        benefactor.transit/deserialize))))

(deftest transit->edn->transit
  (is (= "[\"^ \",\"a\",\"value-a\"]"
    (-> "[\"^ \",\"a\",\"value-a\"]"
        benefactor.transit/deserialize
        benefactor.transit/serialize))))
