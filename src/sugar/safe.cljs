(ns sugar.safe
  (:refer-clojure :exclude [keyword re-seq]))

(defn re-seq
  [re s]
  (when (and re s)
    (clojure.core/re-seq re s)))

(defn keyword
  ([name] (when name (clojure.core/keyword name)))
  ([ns name] (when (and ns name) (clojure.core/keyword ns name))) )
