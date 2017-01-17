(ns github-client.page.exploration
  (:require
    [cljs.pprint :as pprint]
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
        url (cell= (get urls url-id))
        placeholders (cell= (safe/re-seq #"\{.*?\}" url))
        indexed-placeholders (cell= (map-indexed vector placeholders))
        placeholder-map (cell= (->> indexed-placeholders
                                    (map (fn [[idx ph]] {ph (safe/keyword (:url-id route) (str idx))}))
                                    (reduce merge {})))
        raw? (cell true)
        error (cell= (get (db/get-app db :flash-error) (or url-id ::not-found)))]
    (h/div
      (h/h3 (h/text "~{url-id}"))
      (h/h4 (h/text "~{url}"))
      (when-tpl error
        (s/columns
          (h/div :column 12
            (s/toast-error
              (s/button-clear
                :class "float-right"
                :click #(dispatch queue [:clear-flash-error [@url-id]]))
              (h/text "~(with-out-str (pprint/pprint error))")))))
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
              :click #(reset! raw? true)
              :css {:cursor "pointer"}
              :options (cell= (if raw? #{:active} #{}))
              "Raw")
            (s/tab-item
              :options (cell= (if (not raw?) #{:active} #{}))
              :css {:cursor "pointer"}
              :click #(reset! raw? false)
              "Table"))
          (if-tpl raw?
            (h/div
              (h/pre
                (h/text "~(with-out-str (pprint/pprint (get (db/get-app db :exploration) (or url-id ::not-found))))")))
            (h/div
              (cell= (benefactor.json-html/render (get (db/get-app db :exploration) (or url-id ::not-found)))))))))))
