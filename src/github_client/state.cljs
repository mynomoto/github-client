(ns github-client.state
  (:require
    [benefactor.datascript.form :as form]
    [benefactor.development :as dev]
    [benefactor.local-storage]
    [github-client.db :as db]
    [hoplon.core :as h]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [javelin.datascript]))

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
  "Sets the title of the app from the value of the title cell."
  []
  (h/do-watch title
    (fn [old new]
      (set! (.-title js/document) new))))

(defn same-schema?
  "Checks if the local db schema is the same as the current db schema."
  [schema]
  (= (benefactor.local-storage/get :db-schema) schema))

(defn update-local-schema
  "Updates the local db schema with the current db schema"
  [schema]
  (dev/when-debug
    (console.log :updated-local-storage-db-schema))
  (benefactor.local-storage/set! :db-schema schema))

(defn restore-db
  "Restore the db from a backup on local storage."
  [db]
  (dev/when-debug
    (console.log :restored-db-from-local-storage))
  (some->> (benefactor.local-storage/get :db-backup)
           db/deserialize-transit
           (reset! db)))

(defn try-restore-db
  "Tries to restore the db if there was no changes in the db schema, else
  update the local db schema."
  [db schema]
  (if (same-schema? schema)
    (restore-db db)
    (update-local-schema schema)))

(defn backup-db
  "Add a watch to backup the db to localstorage everytime it changes."
  [db]
  (h/do-watch db
    (fn [old new]
      (when new
        (benefactor.local-storage/set! :db-backup (db/serialize-transit new))))))
