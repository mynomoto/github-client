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

(defn navigate!
  ([hash-router route] (navigate! hash-router route nil))
  ([hash-router route params] (sugar.route/navigate! hash-router routes route params)))

(defn update-route
  "Save route on db"
  [db data]
  (db/store-app-data db :github-client :app/route data))

(defn init!
  "Initialize route listening."
  [queue]
  (sugar.route/update-route-on-hashchange!
    routes
    #(dispatch queue [:update-route %])))
