(ns github-client.handler.core
  (:require
    [github-client.db :as db]
    [github-client.route :as route]
    [github-client.reducer :refer [dispatch]]
    [benefactor.datascript.form :as form]
    [javelin.datascript]
    [github-client.api :as api]))

(def global
  {:login-submit
   api/login

   :update-profile
   (fn [{:keys [queue]} event]
     (dispatch queue [:login-submit] event)
     (dispatch queue [:navigate [:profile]]) event)

   :clear-form-values
   (fn [{:keys [db]} [_ data]]
     (if (coll? data)
       (apply form/clear-value db data)
       (form/clear-value db data)))

   :set-form-field
   (fn [{:keys [db]} [_ [key field value & kvs]]]
     (apply form/set-value db key field value kvs)
     (apply form/set-dirty db key field value kvs))

   :set-form-error
   (fn [{:keys [db]} [_ [key field value & kvs]]]
     (apply form/set-error db key field value kvs))

   :clear-form-errors
   (fn [{:keys [db]} [_ data]]
     (if (coll? data)
       (do
         (apply form/clear-error db data)
         (apply form/clear-dirty db data))
       (do
         (form/clear-error db data)
         (form/clear-dirty db data))))

   :navigate
   (fn [{:keys [db]} [_ data]]
     (apply route/navigate! db data))

   :update-route
   (fn [{:keys [db]} [_ data]]
     (route/update-route db data))

   :store-app-data
   (fn [{:keys [db]} [_ [key field value]]]
     (db/store-app-data db key field value))

   :clear-app-data
   (fn [{:keys [db]} [_ [key field]]]
     (db/clear-app-data db key field))

   :update-local-data
   (fn [{:keys [db]} [_ [form-id keys lookup-ref]]]
     (db/submit-data db form-id keys lookup-ref))

   :init
   (fn [{:keys [queue]} _]
     (dispatch queue [:store-app-data [:github-client :page/title "Hoplon Github Client"]]))

   :explore
   api/exploration

   :done
   (fn [{:keys [db]} [_ done-event]]
     (javelin.datascript/set-remove db [:app/id :github-client] :loading done-event))

   :loading
   (fn [{:keys [db]} [_ loading-event]]
     (javelin.datascript/set-add db [:app/id :github-client] :loading loading-event))

   :show-flash-error
   (fn [{:keys [db]} [_ [field value]]]
     (db/store-app-data db :flash-error field value))

   :clear-flash-error
   (fn [{:keys [db]} [_ [field]]]
     (db/clear-app-data db :flash-error field))
   })
