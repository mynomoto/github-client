(ns benefactor.tracking-test
  (:require
    [benefactor.tracking]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest track
  (let [tracked0 (benefactor.tracking/track {})
        tracked1 (benefactor.tracking/track tracked0)
        tracked2 (benefactor.tracking/track tracked1)]
  (is (string? (benefactor.tracking/get-track tracked0)))
  (is (= 3 (count (benefactor.tracking/get-track tracked0))))
  (is (= 7 (count (benefactor.tracking/get-track tracked1))))
  (is (= 11 (count (benefactor.tracking/get-track tracked2))))
  (is (= (benefactor.tracking/get-track tracked0)
         (subs (benefactor.tracking/get-track tracked1) 0 3)
         (subs (benefactor.tracking/get-track tracked2) 0 3)))
  (is (= (benefactor.tracking/get-track tracked1)
         (subs (benefactor.tracking/get-track tracked2) 0 7)))))
