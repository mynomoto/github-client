(ns cards.core
  (:require
    [cljs.core.async :as async]
    [github-client.view.layout :as layout]
    [goog.object :as obj]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.jquery]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn positions
  [pred coll]
  (keep-indexed
    (fn [idx x]
      (when (pred x)
        idx))
    coll))

(defonce card-register (cell []))

(defn card
  [header & body]
  (s/card
    (s/card-header
      (h/h4 :class "card-title"
        header))
    (s/card-body
      body
      )
    (s/card-footer
      )))

(defn upsert-card
  [register card]
  (if-let [idx (first (positions #(= (:id %) (:id card)) register))]
    (assoc register idx card)
    (conj register card)))

(defn add-to-register
  [card]
  (swap! card-register upsert-card card))

(defn show []
  (layout/container
    :id "cards"
    (mapv #((:card %)) @card-register)
    ))

(def button-card
  (add-to-register
    {:id ::button
     :card #(let [queue (async/chan 5)]
             (card "Buttons"
               (h/div :class "container"
                 (h/div :column-xs 10
                   (s/button
                     "Default")
                   (s/button-primary
                     "Primary")
                   (s/button-link
                     "Link")))))}))

(def navbar-card
  (add-to-register
    {:id ::navbar
     :card #(let [queue (async/chan 5)]
             (card "Navbar"
               (layout/navbar {:queue queue})))}))

(defn ^:export reload []
  (js/jQuery #(.replaceWith (js/jQuery "#cards") (show))))

(defn init! []
  (reload))
