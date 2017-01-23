(ns github-client.route
  (:require
    [benefactor.route]
    [bidi.bidi :as bidi]
    [datascript.core :as d]
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(def routes
  ["" [["" :index]
       ["/"
        [["" :index]
         ["profile" :profile]
         [["profile/" "edit"] :profile-edit]
         [["api/" "rate-limit/" [#"show|raw|table" :display]] :rate-limit]
         ["version" :app-version]
         [["exploration/" :url-id "/" [#"raw|table" :display]] :exploration]
         [true :not-found]]]]])

(defn href
  ([route-name] (href route-name nil))
  ([route-name params] (benefactor.route/route->path routes route-name params)))

(defn path->route
  ([path] (benefactor.route/path->route routes path)))

(defn update-route
  "Save route on db"
  [db [path route]]
  (db/store-app-data db :github-client
                     :app/path path
                     :app/route route))

(defn navigate!
  ([db route-name]
   (navigate! db route-name nil))
  ([db route-name params]
   (let [path (href route-name params)
         parsed-route (path->route path)]
     (if parsed-route
       (db/store-app-data db :github-client
                          :app/path path
                          :app/route parsed-route)
       (console.error ::route-not-found path)))))

(defn init!
  "Initialize route listening."
  [queue current-route]
  (benefactor.route/setup-router
    routes
    #(when (not= @current-route (first %)) (dispatch queue [:update-route %]))
    #(dispatch queue [:route-not-found (benefactor.route/path->hash %)])))
