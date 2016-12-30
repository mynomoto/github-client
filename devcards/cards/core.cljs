(ns cards.core
  (:require
    [cljs.core.async :as async]
    [devcards.core :include-macros true :refer-macros [defcard dom-node]]
    [github-client.view.layout :as layout]
    [hoplon.jquery]))

(enable-console-print!)

(def my-queue (async/chan 5))

(defcard navbar
  (dom-node
    (fn [data-atom node]
      #_(.replaceWith (js/jQuery "#app") (page/show {:db state/db :queue reducer/queue :hash-router hash-router :history state/history :selected-history state/selected-history}))
      (.replaceWith (js/jQuery node) (layout/navbar {:queue my-queue})))))

(defn main []
  (devcards.core/start-devcard-ui!))
