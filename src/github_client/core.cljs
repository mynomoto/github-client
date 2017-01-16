(ns github-client.core
  (:require
    [github-client.db :as db]
    [github-client.handler.core :as handler]
    [github-client.page.core :as page]
    [github-client.reducer :as reducer]
    [github-client.route :as route]
    [github-client.state :as state]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.jquery]
    [httpurr.client.xhr :as xhr]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defonce path
  (cell= (:app/path (db/get-app state/db :github-client))))

(defonce router
  (route/init! reducer/queue path))

(defn start-reducer!
  []
  (reducer/start! handler/global
    {:db state/db
     :http xhr/client
     :queue reducer/queue}
    {:history state/history
     :selected-history state/selected-history}))

(defn ^:export reload []
  (reducer/stop! reducer/queue)
  (start-reducer!)
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
  (start-reducer!)
  (reducer/dispatch reducer/queue [:init])

  (benefactor.route/sync-from-atom router path)

  (reload))
