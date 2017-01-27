(ns github-client.page.emoji
  (:require
    [benefactor.json-html]
    [benefactor.search]
    [clojure.string :as str]
    [cljs.pprint :as pprint]
    [cljs-time.coerce]
    [cljs-time.core]
    [cljs-time.format]
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [github-client.view.layout :as layout]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn- current-page
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

(defn show
  [{:keys [route db queue]}]
  (let [url-id (cell :emojis_url)
        url (cell= (get (:app/url (db/get-app db :github-client)) url-id))
        data (cell= (get (db/get-app db :api) (or url-id ::not-found)))
        display (cell= (display route))
        per-page (cell= (per-page route))
        search (cell= (search route))
        filtered-data (cell= (sort (filter #(benefactor.search/match? search (name (first %))) data)))
        paginated-data  (cell= (paginate per-page filtered-data))
        last-page  (cell= (count paginated-data))
        page (cell= (-> route :query-params (get "page" "1") js/parseInt (min last-page) (max 1)))
        paged-data* (cell= (current-page paginated-data page))
        paged-data (cell [])
        loading? (cell= (some #{[:explore [url-id url]]} (:loading (db/get-app db :github-client))))
        error (cell= (get (db/get-app db :flash-error) (or url-id ::not-found)))]
    (cell= (when paged-data*
             (reset! ~(cell paged-data) (map (fn [_] ["" "loading.gif"]) (range (count paged-data*))))
             (h/with-timeout 0
               (reset! ~(cell paged-data) paged-data*))))
    (cell= (when (and (not data)
                      (= (:handler route) :emoji))
             (dispatch queue [:explore [url-id url]])))
    (h/div
      (h/h3 "Emoji")
      (h/h4 (h/text "~{url}"))
      (layout/flash-error error url-id queue)
      (h/form
        (s/form-group
          (s/button-primary
            :options (cell= (if loading? #{:loading} #{}))
            :click #(dispatch queue [:explore [@url-id @url]])
            "Refresh")))
      (when-tpl (cell= (get (db/get-app db :api) (or url-id ::not-found)))
        (h/div
          (s/tab :options #{:block}
            (s/tab-item
              :click #(dispatch queue [:navigate [:emoji {:query-params {:display "show"}}]])
              :css {:cursor "pointer"}
              :options (cell= (if (= display "show") #{:active} #{}))
              "Show")
            (s/tab-item
              :click #(dispatch queue [:navigate [:emoji {:query-params {:display "raw"}}]])
              :css {:cursor "pointer"}
              :options (cell= (if (= display "raw") #{:active} #{}))
              "Raw")
            (s/tab-item
              :click #(dispatch queue [:navigate [:emoji {:query-params {:display "table"}}]])
              :css {:cursor "pointer"}
              :options (cell= (if (= display "table") #{:active} #{}))
              "Table"))
          (case-tpl display
            "show"
            (h/div
              (h/form
                (s/form-group
                  (s/form-label "Search")
                  (s/input
                    :type "text"
                    :value search
                    :keyup #(dispatch queue [:navigate [:emoji {:query-params {:display "show" :search @%}}]])
                    :placeholder "Search")))
              (layout/pagination page last-page per-page queue search :emoji)
              (s/table :options #{:striped :hover}
                (h/thead
                  (h/tr
                    (h/th "Identifier")
                    (h/th "Emoji")
                    (h/th "URL")))
                (h/tbody
                  (for-tpl [[key src] paged-data]
                           (h/tr
                             (h/td (h/text "~(some-> key name)"))
                             (h/td (s/img :css {:width "64" :height "64"} :src src))
                             (h/td (h/text "~{src}"))))))
              (layout/pagination page last-page per-page queue search :emoji))

            "raw"
            (h/div
              (h/pre
                (h/text "~(with-out-str (pprint/pprint data))")))

            "table"
            (h/div
              (layout/pagination page last-page per-page queue search :emoji {:query-params {:display "table"}})
              (cell= (benefactor.json-html/render (into {} paged-data)))
              (layout/pagination page last-page per-page queue search :emoji {:query-params {:display "table"}}))))))))
