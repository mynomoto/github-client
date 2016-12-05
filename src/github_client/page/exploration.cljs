(ns github-client.page.exploration
  (:require
    [cljs.pprint :as pprint]
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn show
  [{:keys [route db queue]}]
  (let [app (cell= (db/get-app db))
        urls (cell= (:app/url app))
        url-id (cell= (keyword (:url-id route)))
        url (cell= (when url-id (url-id urls)))
        placeholders (cell= (when url (re-seq #"\{.*?\}" url)))
        raw? (cell true)]
    (h/div
      (h/h3 (h/text "~{url-id}"))
      (h/h4 (h/text "~{url}"))
      (h/form
        (for-tpl [[idx ph] (cell= (map-indexed (fn [idx item] [idx item]) placeholders))]
          (let [id (cell= (if url-id (keyword (:url-id route) (str idx)) :not-found))]
            (s/form-group
              (s/form-label (h/text "~{ph}"))
              (s/input
                :value (cell= (when-not (= id :not-found) (id (db/get-form db ::exploration))))
                :change (fn [e]
                          (dispatch queue [:store-form-field [::exploration @id @e]]))
                :keypress (fn [e]
                            (when (= (sugar.keycodes/to-code :enter)
                                    (sugar.keycodes/event->code e))
                              (dispatch queue [:store-form-field [::exploration @id @e]])
                              (dispatch queue [:explore [@url-id @url]])))
                :type "text"))))
        (s/form-group
         (s/button-primary
           :click #(dispatch queue [:explore [@url-id @url]])
           "Explore")))
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
            (h/text "~(when url-id (with-out-str (pprint/pprint (url-id (db/get-app* db :exploration)))))")))
        (h/div
          (cell= (when url-id (sugar.json-html/render (url-id (db/get-app* db :exploration))))))))))
