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

(defn page-link
  ([t click active?]
   (page-link t click active? true false))
  ([t click active? show?]
   (page-link t click active? show? false))
  ([t click active? show? disabled?]
   (when-tpl show?
     (let [attributes {:class (cell= {:disabled disabled?})
                       :options (cell= (if active? #{:active} #{}))
                       :href ""}]
       (s/page-item
         (if click
           (assoc attributes :click (fn [event]
                                      (.preventDefault event)
                                      (click)))
           attributes)
         (h/text "~{t}"))))))

(defn pagination
  [page last-page per-page queue search]
  (let [hfn (fn [f page]
              #(let [go-to-page (f @page)]
                 (dispatch queue [:navigate [:emoji {:query-params {:display "show"
                                                                    :search @search
                                                                    :page go-to-page
                                                                    :per-page @per-page}}]])))]
    (s/pagination
      (page-link "Previous" (hfn #(max 1 (dec %)) page) false true (cell= (= 1 page)))
      (page-link "1" (hfn (fn [x] 1) page) false (cell= (not= page 1)))
      (page-link "…" nil false (cell= (> (- page 3) 1)) true)
      (page-link (cell= (- page 2)) (hfn #(- % 2) page) false (cell= (> (- page 2) 1)))
      (page-link (cell= (dec page)) (hfn #(dec %) page) false (cell= (> (dec page) 1)))
      (page-link page nil true)
      (page-link (cell= (inc page)) (hfn #(inc %) page) false (cell= (< (inc page) last-page)))
      (page-link (cell= (+ page 2)) (hfn #(+ % 2) page) false (cell= (< (+ page 2) last-page)))
      (page-link "…" nil false (cell= (< (+ page 3) last-page)) true)
      (page-link last-page (hfn identity last-page) false (cell= (and (< page last-page) (not= 1 last-page))))
      (page-link "Next" (hfn #(min @last-page (inc %)) page) false true
        (cell= (>= page last-page))))))

(defn is-substring?
  [string substring]
  (when (and (string? string) (string? substring))
    (not= -1 (.indexOf string substring))))

(defn search-match
  [search-term]
  (let [lc-search-term (str/lower-case search-term)]
    (fn [[key val]]
      (if (<= 1 (count lc-search-term))
        (is-substring? key lc-search-term)
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
        page (cell= (-> route :query-params (get "page" "1") js/parseInt))
        per-page (cell= (-> route :query-params (get "per-page" "20") js/parseInt))
        search (cell= (-> route :query-params (get "search" "")))
        filtered-data (cell= (sort (filter (search-match search) data)))
        paginated  (cell= (vec (partition-all per-page filtered-data)))
        last-page  (cell= (count paginated))
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
              (pagination page last-page per-page queue search))

            "raw"
            (h/div
              (h/pre
                (h/text "~(with-out-str (pprint/pprint data))")))

            "table"
            (h/div
              (cell= (benefactor.json-html/render data)))))))))
