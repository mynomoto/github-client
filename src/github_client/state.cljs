(ns github-client.state
  (:require
    [github-client.db :as db]
    [hoplon.core :as h]
    [javelin.datascript]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [benefactor.datascript.form :as form]
    [benefactor.local-storage]))

(def schema
  "Schema for the central state storage"
  {:app/id {:db/unique :db.unique/identity}
   :user/id {:db/unique :db.unique/identity}
   ::form/value {:db/unique :db.unique/identity}
   ::form/error {:db/unique :db.unique/identity}
   ::form/dirty {:db/unique :db.unique/identity}})

(defonce db (javelin.datascript/create-conn schema))

(defonce history (cell []))

(defonce selected-history (cell nil))

(defc= title
  (:page/title (db/get-app db :github-client)))

(defn start-sync-title
  []
  (h/do-watch title
    (fn [old new]
      (set! (.-title js/document) new))))

(defn restore-and-watch-db
  []
  (some->> (benefactor.local-storage/get :db-backup)
             db/deserialize-transit
             (reset! db))
  (h/do-watch db
      (fn [old new]
        (when new
          (benefactor.local-storage/set! :db-backup (db/serialize-transit new))))))
