(ns benefactor.json-test
  (:require
    [benefactor.json]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest edn->json->edn
  (is (= {:a "value-a"}
    (-> {:a "value-a"}
        benefactor.json/serialize
        benefactor.json/deserialize))))

(deftest json->edn->json
  (is (= "{\"a\":\"value-a\"}"
    (-> "{\"a\":\"value-a\"}"
        benefactor.json/deserialize
        benefactor.json/serialize))))

(deftest edn->transit->edn
  (is (= {"a" "value-a"}
    (-> {"a" "value-a"}
        benefactor.json/serialize-t
        benefactor.json/deserialize-t))))

(deftest transit->edn->transit
  (is (= "{\"a\":\"value-a\"}"
    (-> "{\"a\":\"value-a\"}"
        benefactor.json/deserialize-t
        benefactor.json/serialize-t))))
