(ns github-client.handler.core
  (:require
    [github-client.db :as db]
    [github-client.route :as route]
    [github-client.reducer :refer [dispatch]]
    [benefactor.datascript.form :as form]
    [github-client.api :as api]))

(def global
  {:login-submit
   (fn [{:keys [db queue current-route]} data]
     (api/login db queue current-route))

   :update-profile
   (fn [{:keys [queue]} data]
     (dispatch queue [:login-submit])
     (dispatch queue [:navigate [:profile]]))

   :clear-form-values
   (fn [{:keys [db]} data]
     (if (coll? data)
       (apply form/clear-value db data)
       (form/clear-value db data)))

   :set-form-field
   (fn [{:keys [db]} [key field value & kvs]]
     (apply form/set-value db key field value kvs)
     (apply form/set-dirty db key field value kvs))

   :set-form-error
   (fn [{:keys [db]} [key field value & kvs]]
     (apply form/set-error db key field value kvs))

   :clear-form-errors
   (fn [{:keys [db]} data]
     (if (coll? data)
       (do
         (apply form/clear-error db data)
         (apply form/clear-dirty db data))
       (do
         (form/clear-error db data)
         (form/clear-dirty db data))))

   :navigate
   (fn [{:keys [db]} data]
     (apply route/navigate! db data))

   :update-route
   (fn [{:keys [db]} data]
     (route/update-route db data))

   :store-app-data
   (fn [{:keys [db]} [key field value]]
     (db/store-app-data db key field value))

   :clear-app-data
   (fn [{:keys [db]} [key field]]
     (db/clear-app-data db key field))

   :update-local-data
   (fn [{:keys [db]} [form-id keys lookup-ref]]
     (db/submit-data db form-id keys lookup-ref))

   :init
   (fn [{:keys [queue]} data]
     (dispatch queue [:store-app-data [:github-client :page/title "Hoplon Github Client"]]))

   :explore
   (fn [{:keys [queue db]} [url-id url]]
     (api/exploration url-id url db queue))})
