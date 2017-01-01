(ns github-client.route
  (:require
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [datascript.core :as d]
    [github-client.reducer :refer [dispatch]]
    [github-client.db :as db]
    [sugar.route]))

(def routes
  (sugar.route/create
    [[:index [[]]]
     [:profile [["profile"]]]
     [:profile-edit [["profile" "edit"]]]
     [:exploration [["exploration" :url-id]]]]))

(defn href
  ([route] (href route nil))
  ([route params] (sugar.route/href routes route params)))

(defn path->route
  ([path] (sugar.route/path->route routes path)))

(defn update-route
  "Save route on db"
  [db [path route]]
  (db/store-app-data db :github-client
                     :app/path path
                     :app/route (dissoc route :domkm.silk/routes :domkm.silk/url)))

(defn navigate!
  ([db route]
   (navigate! db route nil))
  ([db route params]
   (let [path (sugar.route/hash->path (href route params))
         parsed-route (path->route path)]
     (if parsed-route
       (db/store-app-data db :github-client
                          :app/path path
                          :app/route (dissoc parsed-route :domkm.silk/routes :domkm.silk/url))
       (console.error ::route-not-found path)))))

(defn init!
  "Initialize route listening."
  [queue current-route]
  (sugar.route/update-route-on-hashchange!
    routes
    #(when (not= @current-route (first %)) (dispatch queue [:update-route %]))))
