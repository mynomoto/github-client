(ns github-client.db
  (:require
    [cljs.reader]
    [javelin.datascript :as d]
    [datascript.transit :as dt]
    [sugar.datascript.form :as form]))

(defn get-app
  [db id]
  (d/touch (d/entity db [:app/id id])))

(defn get-user
  [db]
  (d/touch (d/entity db [:user/id :github-client])))

(defn store-app-data
  ([db id k v]
   (d/transact! db [{:app/id id
                     k v}]))
  ([db id k v & kvs]
   (d/transact! db [(reduce (fn [[acc ok ov]]
                              (assoc acc ok ov))
                      {:app/id id k v}
                      (partition 2 kvs))])))

(defn clear-app-data
  [db id korks]
  (if (sequential? korks)
    (d/transact! db (mapv (fn [k] [:db.fn/retractAttribute [:app/id id] k]) korks))
    (d/transact! db [[:db.fn/retractAttribute [:app/id id] korks]])))

(defn submit-data
  [db form-id keys lookup-ref]
  (let [form (form/values @db form-id keys)]
    (d/transact! db [(apply assoc form  lookup-ref)])))

(defn serialize-transit
  "Serialize the central state storage"
  [conn]
  (dt/write-transit-str conn))

(defn deserialize-transit
  "Deserialize the central state storage"
  [str]
  (dt/read-transit-str str))

(defn serialize-edn
  [conn]
  (pr-str conn))

(defn deserialize-edn
  [conn]
  (cljs.reader/read-string conn))
