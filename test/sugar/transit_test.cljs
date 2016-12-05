(ns sugar.transit-test
  (:require
    [sugar.transit]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest edn->transit->edn
  (is (= {:a "value-a"}
    (-> {:a "value-a"}
        sugar.transit/serialize
        sugar.transit/deserialize))))

(deftest transit->edn->transit
  (is (= "[\"^ \",\"a\",\"value-a\"]"
    (-> "[\"^ \",\"a\",\"value-a\"]"
        sugar.transit/deserialize
        sugar.transit/serialize))))
