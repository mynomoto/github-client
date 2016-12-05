(ns github-client.handler.core
  (:require
    [github-client.db :as db]
    [github-client.route :as route]
    [github-client.reducer :refer [dispatch]]
    [github-client.api :as api]))

(def global
  {:login-submit
   (fn [{:keys [db queue route]} data]
     (api/login db queue route))

   :reset-form
   (fn [{:keys [db]} data]
     (db/reset-form db data))

   :update-profile
   (fn [{:keys [queue]} data]
     (dispatch queue [:login-submit])
     (dispatch queue [:navigate [:profile]]))

   :store-form-field
   (fn [{:keys [db]} [key field value]]
     (db/store-form-field db key field value))

   :store-form-error
   (fn [{:keys [db]} [key field value & kvs]]
     (apply db/store-form-error db key field value kvs))

   :navigate
   (fn [{:keys [db]} data]
     (apply route/navigate! data))

   :store-app-data
   (fn [{:keys [db]} [key field value]]
     (db/store-app-data db key field value))

   :update-local-data
   (fn [{:keys [db]} [form-id keys lookup-ref]]
     (db/submit-data db form-id keys lookup-ref))

   :init
   (fn [{:keys [queue]} data]
     (dispatch queue [:store-app-data [:github-client :page/title "Hoplon Github Client"]]))

   :explore
   (fn [{:keys [queue db route]} [url-id url]]
     (api/exploration url-id url db queue))})
