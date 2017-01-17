(ns github-client.api
  (:require
    [benefactor.http]
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [github-client.route :as route]
    [github-client.state :as state]
    [goog.crypt.base64 :as base64]
    [goog.object :as obj]
    [httpurr.client :as http]
    [httpurr.client.xhr :refer [client]]
    [httpurr.status :as status]
    [medley.core :as medley]
    [promesa.core :as p]
    [benefactor.datascript.form :as form]
    [benefactor.json]))

(def ^:const api-host "https://api.github.com")

(defn default-headers
  [username token]
  (medley/assoc-some {"Content-Type" "application/json"
                      "Accept" "application/vnd.github.v3+json"}
                     "Authorization" (when (and username token)
                                       (benefactor.http/basic-auth-header username token))))

(defn default-request-map
  [username token]
  {:method :get
   :headers (default-headers username token)})

(defn request
  "Updates the body of a request to convert it to a json string."
  [http request-map loading-fn done-fn]
  (benefactor.http/request http
    (merge-with merge
      (let [user (db/get-user @state/db)]
        (default-request-map (:user/username user) (:user/token user)))
      (benefactor.http/json-serialize-request-body request-map))
    loading-fn
    done-fn))

(defn login-request
  "Do a login request"
  [http request-map loading-fn done-fn]
  (benefactor.http/request http
    (merge-with merge
      (let [form-user (form/values @state/db :github-client.page.login/login)
            user (db/get-user @state/db)]
        (default-request-map (:user/username form-user (:user/username user)) (:user/token form-user (:user/token user))))
      (benefactor.http/json-serialize-request-body request-map))
    loading-fn
    done-fn))

(defn login
  [{:keys [db http queue current-route]} event]
  (-> (p/chain
        (login-request http {:url api-host}
          #(dispatch queue [:loading event] event)
          #(dispatch queue [:done event] event))
        benefactor.http/json-deserialize-response-body
        benefactor.http/reject-response-if-not-success
        :body
        (fn [body]
          (dispatch queue [:update-local-data [:github-client.page.login/login [:user/token :user/username] [:user/id :github-client]]] event)
          (dispatch queue [:store-app-data [:github-client :app/url body]] event)
          (dispatch queue [:clear-form-errors :github-client.page.login/login] event)
          (let [route (:domkm.silk/name @current-route)]
            (cond
              (#{:login} route) (dispatch queue [:navigate [:index]] event)
              (#{:profile-edit} route) (dispatch queue [:navigate [:profile]] event)
              :else nil))))
      (p/catch (fn [err]
                 (console.log ::request-failed (obj/get err "response"))
                 (dispatch queue [:set-form-error [:github-client.page.login/login :user/token (-> err :body :message) :user/username (-> err :body :message)]] event)))))

(defn exploration
  [{:keys [db http queue]} [_ [url-id url] :as event]]
  (-> (p/chain
        (request http {:url url}
          #(dispatch queue [:loading event] event)
          #(dispatch queue [:done event] event))
        benefactor.http/json-deserialize-response-body
        benefactor.http/reject-response-if-not-success
        :body
        (fn [body]
          (dispatch queue [:store-app-data [:exploration url-id body]] event)))
      (p/catch (fn [err]
                 (dispatch queue [:show-error (obj/get err "response")] event)))))
