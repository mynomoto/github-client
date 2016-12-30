(ns github-client.core
  (:require
    [devtools.core]
    [github-client.page.core :as page]
    [github-client.reducer :as reducer]
    [github-client.handler.core :as handler]
    [github-client.route :as route]
    [github-client.state :as state]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.jquery]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defonce hash-router (route/init! reducer/queue))

(defonce
  _one-time-side-effects
  (do
    (enable-console-print!)
    (devtools.core/install! [:custom-formatters :hints :async])
    (state/restore-and-watch-db)
    (state/start-sync-title)
    (h/do-watch state/title
      (fn [old new]
        (set! (.-title js/document) new)))
    (reducer/start! handler/global state/db reducer/queue hash-router state/history state/selected-history)
    (reducer/dispatch reducer/queue [:init])
    ))

(defn init! []
  (reducer/stop! reducer/queue)
  (reducer/start! handler/global state/db reducer/queue hash-router state/history state/selected-history)
  (js/jQuery #(.replaceWith (js/jQuery "#app") (page/show {:db state/db :queue reducer/queue :hash-router hash-router :history state/history :selected-history state/selected-history}))))

(init!)
