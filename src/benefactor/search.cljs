(ns benefactor.search
  (:require
    [clojure.string :as str]))

(defn is-substring?
  [string substring]
  (when (and (string? string) (string? substring))
    (not= -1 (.indexOf string substring))))

(defn match?
  [search-term string]
  (let [lc-search-term (str/lower-case search-term)]
    (if (<= 1 (count lc-search-term))
      (is-substring? string lc-search-term)
      true)))
