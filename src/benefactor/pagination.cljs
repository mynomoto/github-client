(ns benefactor.pagination)

(defn current-page
  [paginated-vector page]
  (get paginated-vector (dec page)))

(defn display
  [route]
  (-> route :query-params (get "display")))

(defn per-page
  [route]
  (-> route :query-params (get "per-page" "20") js/parseInt))

(defn search
  [route]
  (-> route :query-params (get "search" "")))

(defn paginate
  [per-page data]
  (vec (partition-all per-page data)))
