(ns github-client.page.exploration
  (:require
    [cljs.pprint :as pprint]
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [sugar.json-html]
    [sugar.safe :as safe]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn show
  [{:keys [route db queue]}]
  (let [urls (cell= (:app/url (db/get-app db)))
        url-id (cell= (keyword (:url-id route)))
        url (cell= (get urls url-id))
        placeholders (cell= (safe/re-seq #"\{.*?\}" url))
        raw? (cell true)]
    (h/div
      (h/h3 (h/text "~{url-id}"))
      (h/h4 (h/text "~{url}"))
      (h/form
        (for-tpl [[idx ph] (cell= (map-indexed (fn [idx item] [idx item]) placeholders))]
          (let [id (cell= (safe/keyword (:url-id route) (str idx)))]
            (s/form-group
              (s/form-label (h/text "~{ph}"))
              (s/input
                :value (cell= (get (db/get-form db ::exploration) (or id :not-found)))
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
            (h/text "~(with-out-str (pprint/pprint (get (db/get-app* db :exploration) (or url-id :not-found))))")))
        (h/div
          (cell= (sugar.json-html/render (get (db/get-app* db :exploration) (or url-id :not-found)))))))))
