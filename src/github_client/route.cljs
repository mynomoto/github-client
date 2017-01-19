(ns github-client.route
  (:require
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [datascript.core :as d]
    [domkm.silk :as silk]
    [github-client.reducer :refer [dispatch]]
    [github-client.db :as db]
    [benefactor.route]))

(def routes
  (benefactor.route/create
    [[:index [[]]]
     [:profile [["profile"]]]
     [:profile-edit [["profile" "edit"]]]
     [:rate-limit [["api" "rate-limit" (silk/? :display {:display "show"})]]]
     [:exploration [["exploration" :url-id (silk/? :display {:display "raw"})]]]]))

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
                     :app/route (dissoc route :domkm.silk/routes :domkm.silk/url :domkm.silk/pattern)))

(defn navigate!
  ([db route-name]
   (navigate! db route-name nil))
  ([db route-name params]
   (let [path (benefactor.route/hash->path (href route-name params))
         parsed-route (path->route path)]
     (if parsed-route
       (db/store-app-data db :github-client
                          :app/path path
                          :app/route (dissoc parsed-route :domkm.silk/routes :domkm.silk/url :domkm.silk/pattern))
       (console.error ::route-not-found path)))))

(defn init!
  "Initialize route listening."
  [queue current-route]
  (benefactor.route/setup-router
    routes
    #(when (not= @current-route (first %)) (dispatch queue [:update-route %]))))
