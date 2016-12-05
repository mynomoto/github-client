(ns github-client.route
  (:require
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [datascript.core :as d]
    [sugar.route]))

(def routes
  (sugar.route/create
    [[:index [[]]]
     [:login [["login"]]]
     [:profile [["profile"]]]
     [:profile-edit [["profile" "edit"]]]
     [:exploration [["exploration" :url-id]]]]))

(defn href
  ([route] (href route nil))
  ([route params] (sugar.route/href routes route params)))

(defn navigate!
  ([route] (navigate! route nil))
  ([route params] (sugar.route/navigate! routes route params)))


(defn init!
  "Initialize route listening."
  [db]
  (sugar.route/update-route-on-hashchange!
    routes
    #(d/transact! db [{:app/id :github-client
                       :app/route %}])))
