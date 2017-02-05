(ns benefactor.safe
  (:refer-clojure :exclude [keyword re-seq name]))

(defn re-seq
  [re s]
  (when (and re s)
    (clojure.core/re-seq re s)))

(defn keyword
  ([name] (when name (clojure.core/keyword name)))
  ([ns name]
   (let [ns (str ns)
         name (str name)]
     (when (and (not-empty ns)
                (not-empty name))
       (clojure.core/keyword ns name)))) )

(defn name
  [x]
  (when x
    (clojure.core/name x)))
