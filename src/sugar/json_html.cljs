(ns sugar.json-html
  (:require
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [clojure.string :as str]))

(defn escape-html [s]
  (str/escape s
    {"&"  "&amp;"
     ">"  "&gt;"
     "<"  "&lt;"
     "\"" "&quot;"}))

(defn render-keyword [k]
  (->> k ((juxt namespace name)) (remove nil?) (str/join "/")))

(defn str-compare [k1 k2]
  (compare (str k1) (str k2)))

(defn sort-map [m]
  (try
    (into (sorted-map) m)
    (catch js/Error _
      (into (sorted-map-by str-compare) m))))

(defn sort-set [s]
  (try
    (into (sorted-set) s)
    (catch js/Error _
      (into (sorted-set-by str-compare) s))))

(declare render)

(defn render-collection [col]
  (if (empty? col)
    (h/div (h/span "Literal: Empty collection"))
    (s/table :options #{:striped :hover}
      (h/tbody
        (for [[i v] (map-indexed vector col)]
          (h/tr
            (h/th i)
            (h/td (render v))))))))

(defn render-set [s]
  (if (empty? s)
    (h/div (h/span "Literal: Empty set"))
    (s/ul
      (for [item (sort-set s)]
       (h/li (render item))))))

(defn render-map [m]
  (if (empty? m)
    (h/div (h/span "Literal: Empty map"))
    (s/table :options #{:striped :hover}
      (h/tbody
        (for [[k v] (sort-map m)]
          (h/tr
            (h/th (render k))
            (h/td (render v))))))))

(defn render-string [s]
  (h/span
    (if (str/blank? s)
      (h/span "\"\"")
      s)))

(defn render [v]
  (let [t (type v)]
    (cond
      (= t Keyword) (h/span (render-keyword v))
      (= t Symbol) (h/span (str v))
      (= t js/String) (h/span v)
      (= t js/Date) (h/span (.toString v))
      (= t js/Boolean) (h/span (str v))
      (= t js/Number) (h/span (str v))
      (satisfies? IMap v) (render-map v)
      (satisfies? ISet v) (render-set v)
      (satisfies? ICollection v) (render-collection v)
      (nil? v) (h/span "nil")
      :else (h/span (pr-str v)))))
