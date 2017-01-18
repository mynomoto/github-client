(ns github-client.page.rate-limit
  (:require
    [benefactor.datascript.form :as form]
    [benefactor.json-html]
    [benefactor.safe :as safe]
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

(defn format-date
  [ms]
  (->> (* ms 1000)
       (cljs-time.coerce/from-long)
       (cljs-time.core/to-default-time-zone)
       (cljs-time.format/unparse (cljs-time.format/formatters :date-time))))

(defn show
  [{:keys [route db queue]}]
  (let [url-id (cell :rate_limit_url)
        url (cell= (get (:app/url (db/get-app db :github-client)) url-id))
        data (cell= (get (db/get-app db :exploration) (or url-id ::not-found)))
        tab (cell :show)
        loading? (cell= (some #{[:explore [url-id url]]} (:loading (db/get-app db :github-client))))
        error (cell= (get (db/get-app db :flash-error) (or url-id ::not-found)))]
    (cell= (when (and (not data)
                      (= (:domkm.silk/name route) :rate-limit))
             (dispatch queue [:explore [url-id url]])))
    (h/div
      (h/h3 "Rate Limit")
      (h/h4 (h/text "~{url}"))
      (layout/flash-error error url-id queue)
      (h/form
        (s/form-group
          (s/button-primary
            :options (cell= (if loading? #{:loading} #{}))
            :click #(dispatch queue [:explore [@url-id @url]])
            "Refresh")))
      (when-tpl (cell= (get (db/get-app db :exploration) (or url-id ::not-found)))
        (h/div
          (s/tab :options #{:block}
            (s/tab-item
              :click #(reset! tab :show)
              :css {:cursor "pointer"}
              :options (cell= (if (= tab :show) #{:active} #{}))
              "Show")
            (s/tab-item
              :click #(reset! tab :raw)
              :css {:cursor "pointer"}
              :options (cell= (if (= tab :raw) #{:active} #{}))
              "Raw")
            (s/tab-item
              :options (cell= (if (= tab :table) #{:active} #{}))
              :css {:cursor "pointer"}
              :click #(reset! tab :table)
              "Table"))
          (case-tpl tab
            :show
            (h/div
              (s/table :options #{:striped :hover}
                (h/thead
                  (h/tr
                    (h/th "Resource")
                    (h/th "Remaining")
                    (h/th "Limit")
                    (h/th "Reset at")))
                (h/tbody
                  (for-tpl [[resource {:keys [limit remaining reset]}] (cell= (:resources data))]
                           (h/tr
                             (h/td (h/text "~{resource}"))
                             (h/td (h/text "~{remaining}"))
                             (h/td (h/text "~{limit}"))
                             (h/td (h/text "~(when reset (format-date reset))")))))))
            :raw
            (h/div
              (h/pre
                (h/text "~(with-out-str (pprint/pprint data))")))

            :table
            (h/div
              (cell= (benefactor.json-html/render data)))))))))
