(ns github-client.core
  (:require
    [github-client.db :as db]
    [github-client.handler.core :as handler]
    [github-client.page.core :as page]
    [github-client.reducer :as reducer]
    [github-client.route :as route]
    [github-client.state :as state]
    [goog.object :as obj]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.jquery]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defonce path
  (cell= (:app/path (db/get-app state/db :github-client))))

(defonce router
  (route/init! reducer/queue path))

(defn ^:export reload []
  (reducer/stop! reducer/queue)
  (reducer/start! handler/global
    {:db state/db
     :queue reducer/queue}
   {:history state/history
    :selected-history state/selected-history})
  (js/jQuery
    #(.replaceWith (js/jQuery "#app")
       (page/show {:db state/db
                   :history state/history
                   :queue reducer/queue
                   :selected-history state/selected-history}))))

(defn init! []
  (enable-console-print!)
  (state/restore-and-watch-db)
  (state/start-sync-title)
  (h/do-watch state/title
    (fn [old new]
      (set! (.-title js/document) new)))
  (reducer/start! handler/global
    {:db state/db
     :queue reducer/queue}
   {:history state/history
    :selected-history state/selected-history})
  (reducer/dispatch reducer/queue [:init])

  (add-watch path ::route-watch
    (fn [k r o n]
      (when (not= o n)
        (.setToken router (benefactor.route/path->token n)))))

  (reload))
