(ns github-client.db
  (:require
    [cljs.reader]
    [datascript.core :as d]
    [datascript.transit :as dt]))

(defn get-form
  [db id]
  (d/entity db [:form/id id]))

(defn get-form-error
  [db id]
  (d/entity db [:form/error id]))

(defn store-form-field
  ([db id k v]
   (d/transact! db [{:form/id id
                     k v}]))
  ([db id k v & kvs]
   (d/transact! db [(reduce (fn [[acc ok ov]]
                              (assoc acc ok ov))
                      {:form/id id k v}
                      (partition 2 kvs))])))

(defn store-app-data
  ([db id k v]
   (d/transact! db [{:app/id id
                     k v}]))
  ([db id k v & kvs]
   (d/transact! db [(reduce (fn [[acc ok ov]]
                              (assoc acc ok ov))
                      {:app/id id k v}
                      (partition 2 kvs))])))

(defn store-form-error
  ([db id k v]
   (d/transact! db [{:form/error id
                     k v}]))
  ([db id k v & kvs]
   (d/transact! db [(reduce (fn [acc [ok ov]]
                              (assoc acc ok ov))
                      {:form/error id k v}
                      (partition 2 kvs))])))

(defn submit-data
  [db form-id keys lookup-ref]
  (let [form (select-keys (d/entity @db [:form/id form-id]) keys)]
    (d/transact! db [(apply assoc form  lookup-ref)])))

(defn reset-form
  ([db form-id]
   (d/transact! db [[:db.fn/retractEntity [:form/id form-id]]]))
  ([db form-id key-or-keys]
   (if (sequential? key-or-keys)
     (d/transact! db (mapv (fn [k] [:db.fn/retractAttribute [:form/id form-id] k]) key-or-keys))
     (d/transact! db [[:db.fn/retractAttribute [:form/id form-id] key-or-keys]]))))

(defn get-user
  [db]
  (d/entity db [:user/id :github-client]))

(defn get-app
  [db]
  (d/entity db [:app/id :github-client]))

(defn get-app*
  [db id]
  (d/entity db [:app/id id]))

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
