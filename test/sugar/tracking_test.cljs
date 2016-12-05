(ns sugar.tracking-test
  (:require
    [sugar.tracking]
    [clojure.test :refer-macros [deftest testing is]]))

(deftest track
  (let [tracked0 (sugar.tracking/track {})
        tracked1 (sugar.tracking/track tracked0)
        tracked2 (sugar.tracking/track tracked1)]
  (is (string? (sugar.tracking/get-track tracked0)))
  (is (= 3 (count (sugar.tracking/get-track tracked0))))
  (is (= 7 (count (sugar.tracking/get-track tracked1))))
  (is (= 11 (count (sugar.tracking/get-track tracked2))))
  (is (= (sugar.tracking/get-track tracked0)
         (subs (sugar.tracking/get-track tracked1) 0 3)
         (subs (sugar.tracking/get-track tracked2) 0 3)))
  (is (= (sugar.tracking/get-track tracked1)
         (subs (sugar.tracking/get-track tracked2) 0 7)))))
