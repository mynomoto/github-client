(ns github-client.page.debug
  (:require
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn show
  [{:keys [route db queue history]}]
  (h/div
    :css {:border-width "1px"
          :border "solid black"}
    (s/form-group
      (h/label
        "Debug")
      (s/button
        :click #(reset! history [])
        "Clear history"
        ))
    (s/table :options #{:striped :hover}
      (h/thead
        (h/tr
          (h/th "Event")))
      (h/tbody
        (for-tpl [{:keys [history-event history-db]} history]
          (h/tr
            (h/td
              :css {:cursor "pointer"}
              :click #(reset! db @history-db)
              (h/text "~(pr-str history-event)"))))))
    ))
