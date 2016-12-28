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
     [:contribute [["contribute"]]]
     [:profile [["profile"]]]
     [:profile-edit [["profile" "edit"]]]
     [:exploration [["exploration" :url-id]]]]))

(defn href
  ([route] (href route nil))
  ([route params] (sugar.route/href routes route params)))

(defn navigate!
  ([route] (navigate! route nil))
  ([route params] (sugar.route/navigate! routes route params)))

(defn update-route
  [db data]
  (db/store-app-data db :github-client :app/route data))

(defn init!
  "Initialize route listening."
  [queue]
  (sugar.route/update-route-on-hashchange!
    routes
    #(dispatch queue [:update-route %])))
