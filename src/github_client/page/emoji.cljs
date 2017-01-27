(ns github-client.page.emoji
  (:require
    [benefactor.json-html]
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

(defn is-substring?
  [string substring]
  (when (and (string? string) (string? substring))
    (not= -1 (.indexOf string substring))))

(defn search-match
  [search-term]
  (let [lc-search-term (str/lower-case search-term)]
    (fn [[key val]]
      (if (<= 1 (count lc-search-term))
        (is-substring? (name key) lc-search-term)
        true))))

(defn- current-page
  [paginated-vector page]
  (get paginated-vector (dec page)))

(defn show
  [{:keys [route db queue]}]
  (let [url-id (cell :emojis_url)
        url (cell= (get (:app/url (db/get-app db :github-client)) url-id))
        data (cell= (get (db/get-app db :api) (or url-id ::not-found)))
        tab (cell= (-> route :query-params (get "display")))
        per-page (cell= (-> route :query-params (get "per-page" "20") js/parseInt))
        search (cell= (-> route :query-params (get "search" "")))
        filtered-data (cell= (sort (filter (search-match search) data)))
        paginated  (cell= (vec (partition-all per-page filtered-data)))
        last-page  (cell= (count paginated))
        page (cell= (-> route :query-params (get "page" "1") js/parseInt (min last-page)))
        _ (cell= (console.log :page page))
        paged-data* (cell= (current-page paginated page))
        paged-data (cell [])
        _ (cell= (when paged-data*
                   (reset! ~(cell paged-data) (map (fn [_] ["" "loading.gif"]) (range (count paged-data*))))
                   (h/with-timeout 0
                     (reset! ~(cell paged-data) paged-data*))))
        loading? (cell= (some #{[:explore [url-id url]]} (:loading (db/get-app db :github-client))))
        error (cell= (get (db/get-app db :flash-error) (or url-id ::not-found)))]
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
              :options (cell= (if (= tab "show") #{:active} #{}))
              "Show")
            (s/tab-item
              :click #(dispatch queue [:navigate [:emoji {:query-params {:display "raw"}}]])
              :css {:cursor "pointer"}
              :options (cell= (if (= tab "raw") #{:active} #{}))
              "Raw")
            (s/tab-item
              :click #(dispatch queue [:navigate [:emoji {:query-params {:display "table"}}]])
              :css {:cursor "pointer"}
              :options (cell= (if (= tab "table") #{:active} #{}))
              "Table"))
          (case-tpl tab
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
              (s/table :options #{:striped :hover}
                (h/thead
                  (h/tr
                    (h/th "Identifier")
                    (h/th "Emoji")))
                (h/tbody
                  (for-tpl [[key src] paged-data]
                           (h/tr
                             (h/td (h/text "~(some-> key name)"))
                             (h/td (s/img :src src))))))
              (layout/pagination page last-page per-page queue search :emoji))

            "raw"
            (h/div
              (h/pre
                (h/text "~(with-out-str (pprint/pprint data))")))

            "table"
            (h/div
              (cell= (benefactor.json-html/render data)))))))))
