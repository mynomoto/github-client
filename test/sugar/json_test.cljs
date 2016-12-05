(ns sugar.json-test
  (:require
    [sugar.json]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest edn->json->edn
  (is (= {:a "value-a"}
    (-> {:a "value-a"}
        sugar.json/serialize
        sugar.json/deserialize))))

(deftest json->edn->json
  (is (= "{\"a\":\"value-a\"}"
    (-> "{\"a\":\"value-a\"}"
        sugar.json/deserialize
        sugar.json/serialize))))

(deftest edn->transit->edn
  (is (= {"a" "value-a"}
    (-> {"a" "value-a"}
        sugar.json/serialize-t
        sugar.json/deserialize-t))))

(deftest transit->edn->transit
  (is (= "{\"a\":\"value-a\"}"
    (-> "{\"a\":\"value-a\"}"
        sugar.json/deserialize-t
        sugar.json/serialize-t))))
