(ns github-client.page.exploration
  (:require
    [cljs.pprint :as pprint]
    [github-client.view.layout :as layout]
    [benefactor.datascript.form :as form]
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [benefactor.json-html]
    [benefactor.safe :as safe]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn show
  [{:keys [route db queue]}]
  (let [urls (cell= (:app/url (db/get-app db :github-client)))
        url-id (cell= (keyword (:url-id route)))
        tab (cell= (:display route))
        url (cell= (get urls url-id))
        placeholders (cell= (safe/re-seq #"\{.*?\}" url))
        indexed-placeholders (cell= (map-indexed vector placeholders))
        placeholder-map (cell= (->> indexed-placeholders
                                    (map (fn [[idx ph]] {ph (safe/keyword (:url-id route) (str idx))}))
                                    (reduce merge {})))
        error (cell= (get (db/get-app db :flash-error) (or url-id ::not-found)))]
    (h/div
      (h/h3 (h/text "~{url-id}"))
      (h/h4 (h/text "~{url}"))
      (layout/flash-error error url-id queue)
      (h/form
        (for-tpl [[idx ph] indexed-placeholders]
          (let [id (cell= (safe/keyword (:url-id route) (str idx)))]
            (s/form-group
              (s/form-label (h/text "~{ph}"))
              (s/input
                :value (cell= (form/value db ::exploration id))
                :change (fn [e]
                          (dispatch queue [:set-form-field [::exploration @id @e]]))
                :keypress (fn [e]
                            (when (= (benefactor.keycodes/to-code :enter)
                                     (benefactor.keycodes/event->code e))
                              (dispatch queue [:set-form-field [::exploration @id @e]])
                              (dispatch queue [:explore [@url-id @url]])))
                :type "text"))))
        (s/form-group
          (s/button-primary
            :click #(dispatch queue [:explore [@url-id @url (when (pos? (count @placeholders)) @placeholder-map)]])
            "Explore")
          (s/button
            :click #(dispatch queue [:clear-app-data [:exploration @url-id]])
            "Clear")))
      (when-tpl (cell= (get (db/get-app db :exploration) (or url-id ::not-found)))
        (h/div
          (s/tab :options #{:block}
            (s/tab-item
              :click #(dispatch queue [:navigate [:exploration {:url-id (name @url-id) :display "raw"}]])
              :css {:cursor "pointer"}
              :options (cell= (if (= tab "raw") #{:active} #{}))
              "Raw")
            (s/tab-item
              :options (cell= (if (= tab "table") #{:active} #{}))
              :css {:cursor "pointer"}
              :click #(dispatch queue [:navigate [:exploration {:url-id (name @url-id) :display "table"}]])
              "Table"))
          (case-tpl tab
            "raw"
            (h/div
              (h/pre
                (h/text "~(with-out-str (pprint/pprint (get (db/get-app db :exploration) (or url-id ::not-found))))")))
            "table"
            (h/div
              (cell= (benefactor.json-html/render (get (db/get-app db :exploration) (or url-id ::not-found)))))))))))
