(ns sugar.datascript.form
  (:require
    [javelin.datascript :as d]
    [sugar.util]))

(defn value
  [db id k]
  (when (and db id k)
    (get (d/entity db [::value id]) k)))

(defn values
  ([db id]
   (d/touch (d/entity db [::value id])))
  ([db id korks]
   (d/select-keys (d/entity db [::value id]) (sugar.util/korks->ks korks))))

(defn error
  [db id k]
  (when (and db id k)
    (get (d/entity db [::error id]) k)))

(defn errors
  [db id]
  (d/touch (d/entity db [::error id])))

(defn dirty?
  [db id k]
  (when (and db id k)
    (get (d/entity db [::dirty id]) k)))

(defn dirties
  [db id]
  (d/touch (d/entity db [::dirty id])))

(defn data
  [db id]
  {:value (values db id)
   :error (errors db id)
   :dirty (dirties db id)})

(defn set-value
  ([db id k v]
   (d/transact! db [{::value id
                     k v}]))
  ([db id k v & kvs]
   (d/transact! db [(reduce (fn [acc [ok ov]]
                              (assoc acc ok ov))
                      {::value id k v}
                      (partition 2 kvs))])))

(defn set-error
  ([db id k v]
   (d/transact! db [{::error id
                     k v}]))
  ([db id k v & kvs]
   (d/transact! db [(reduce (fn [acc [ok ov]]
                              (assoc acc ok ov))
                      {::error id k v}
                      (partition 2 kvs))])))

(defn set-dirty
  ([db id k _]
   (d/transact! db [{::dirty id
                     k true}]))
  ([db id k _ & kvs]
   (d/transact! db [(reduce (fn [acc [ok _]]
                              (assoc acc ok true))
                      {::dirty id k true}
                      (partition 2 kvs))])))

(defn set-pristine
  ([db id k _]
   (d/transact! db [{::dirty id
                     k false}]))
  ([db id k _ & kvs]
   (d/transact! db [(reduce (fn [acc [ok _]]
                              (assoc acc ok false))
                      {::dirty id k false}
                      (partition 2 kvs))])))

(defn clear-value
  ([db id]
   (d/transact! db [[:db.fn/retractEntity [::value id]]]))
  ([db id key-or-keys]
   (if (sequential? key-or-keys)
     (d/transact! db (mapv (fn [k] [:db.fn/retractAttribute [::value id] k]) key-or-keys))
     (d/transact! db [[:db.fn/retractAttribute [::value id] key-or-keys]]))))

(defn clear-error
  ([db id]
   (d/transact! db [[:db.fn/retractEntity [::error id]]]))
  ([db id key-or-keys]
   (if (sequential? key-or-keys)
     (d/transact! db (mapv (fn [k] [:db.fn/retractAttribute [::error id] k]) key-or-keys))
     (d/transact! db [[:db.fn/retractAttribute [::error id] key-or-keys]]))))

(defn clear-dirty
  ([db id]
   (d/transact! db [[:db.fn/retractEntity [::dirty id]]]))
  ([db id key-or-keys]
   (if (sequential? key-or-keys)
     (d/transact! db (mapv (fn [k] [:db.fn/retractAttribute [::dirty id] k]) key-or-keys))
     (d/transact! db [[:db.fn/retractAttribute [::dirty id] key-or-keys]]))))
