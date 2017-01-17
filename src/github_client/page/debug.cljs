(ns github-client.page.debug
  (:require
    [benefactor.tracking]
    [clojure.string :as str]
    [github-client.state :as state]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn format-event-timestamp
  [timestamp]
  (when timestamp
    (str/replace timestamp \T " ")))

(defn show
  [{:keys [route db queue history]}]
  (h/div
    :css {:border-width "1px"
          :border "solid black"}
    (s/form-group
      (h/label
        "Debug ")
      (s/button
        :click #(do
                  (reset! history [])
                  (reset! state/selected-history nil))
        "Clear history"))
    (s/table :options #{:striped :hover}
      (h/thead
        (h/tr
          (h/th "#")
          (h/th "Timestamp")
          (h/th "Track Id")
          (h/th "Event")))
      (h/tbody
        (for-tpl [[idx {:keys [history-event history-db]}] (cell= (map-indexed vector history))]
                 (h/tr
                   :css (cell= (if (= idx state/selected-history)
                                 {:cursor "pointer" :background-color "yellow"}
                                 {:cursor "pointer" :background-color "inherit"}))
                   :click #(do
                             (reset! db @history-db)
                             (reset! state/selected-history @idx))
                   (h/td (h/text "~{idx}"))
                   (h/td (h/text "~(format-event-timestamp (benefactor.tracking/get-timestamp history-event))"))
                   (h/td (h/text "~(benefactor.tracking/get-track history-event)"))
                   (h/td (h/text "~(pr-str history-event)"))))))))
