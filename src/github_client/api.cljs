(ns github-client.api
  (:require
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [github-client.route :as route]
    [github-client.state :as state]
    [goog.crypt.base64 :as base64]
    [httpurr.client :as http]
    [httpurr.client.xhr :refer [client]]
    [httpurr.status :as status]
    [medley.core :as medley]
    [promesa.core :as p]
    [sugar.datascript.form :as form]
    [sugar.json]))

(def ^:const api-host "https://api.github.com")

(defn auth-header
  [username token]
  (when token
    (str "Basic " (base64/encodeString (str username ":" token)))))

(defn default-headers
  [username token]
  (medley/assoc-some {"Content-Type" "application/json"
                      "Accept" "application/vnd.github.v3+json"}
                     "Authorization" (auth-header username token)))

(defn default-request-map
  [username token]
  {:method :get
   :headers (default-headers username token)})

(defn encode-request-body
  "Updates the body of a request to convert it to a json string."
  [request]
  (if (:body request)
    (update request :body sugar.json/serialize)
    request))

(defn request
  "Updates the body of a request to convert it to a json string."
  [request-map]
  (http/send! client
    (merge-with merge
      (let [user (db/get-user @state/db)]
        (default-request-map (:user/username user) (:user/token user)))
      (encode-request-body request-map))))

(defn login-request
  "Do a login request"
  [request-map]
  (http/send! client
    (merge-with merge
      (let [form-user (form/values @state/db :github-client.page.login/login)
            user (db/get-user @state/db)]
        (default-request-map (:user/username form-user (:user/username user)) (:user/token form-user (:user/token user))))
      (encode-request-body request-map))))

(defn process-response
  [response]
  (let [response (update response :body sugar.json/deserialize)]
    (condp = (:status response)
      status/ok           (p/resolved response)
      status/not-found    (p/rejected response)
      status/unauthorized (p/rejected response)
      (p/rejected response))))

(defn login
  [db queue route]
  (-> (p/chain
        (login-request {:url api-host})
        process-response
        :body
        (fn [body]
          (dispatch queue [:update-local-data [:github-client.page.login/login [:user/token :user/username] [:user/id :github-client]]])
          (dispatch queue [:store-app-data [:github-client :app/url body]])
          (let [route (:domkm.silk/name @route)]
            (cond
              (#{:login} route) (dispatch queue [:navigate [:index]])
              (#{:profile-edit} route) (dispatch queue [:navigate [:profile]])
              :else nil))))
      (p/catch (fn [err]
                 (console.log :request-failed err)
                 (dispatch queue [:set-form-error [:github-client.page.login/login :user/token (-> err :body :message) :user/username (-> err :body :message)]])))))

(defn exploration
  [url-id url db queue]
  (-> (p/chain
        (request {:url url})
        process-response
        :body
        (fn [body]
          (dispatch queue [:store-app-data [:exploration url-id body]])))
      (p/catch (fn [err]
                 (dispatch queue [:show-error err])))))
