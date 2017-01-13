(ns benefactor.http
  (:require
    [goog.crypt.base64 :as base64]
    [httpurr.client :as http]
    [httpurr.status :as status]
    [promesa.core :as p]))

(defn basic-auth-header
  "Create the header for basic auth. It should be used with the key
  \"Authorization\" on the request header."
  [username password]
  (str "Basic " (base64/encodeString (str username ":" password))))

(defn request
  "Request using the client an request-map. Optionally accepts a before-fn that
  will be called before the request is send with the request map as argument.
  You can also pass a after-fn that will receive the request map and the result
  as arguments but if you do this the request is not cancelable anymore."
  ([client request-map]
   (http/send! client request-map))
  ([client request-map before-fn]
   (before-fn request-map)
   (http/send! client request-map))
  ([client request-map before-fn after-fn]
   (before-fn request-map)
   (p/branch (http/send! client request-map)
     #(do (after-fn request-map %)
        (p/resolved %))
     #(do (after-fn request-map %)
        (p/rejected %)))))

(defn reject-response-if-not-success
  "Given a reponse reject in case the status is not success."
  [response]
  (cond
    (status/success? response) (p/resolved response)
    :else (p/rejected response)))

(defn json-serialize-request-body
  "Updates the body of a request to convert it to a json string."
  [request]
  (if (:body request)
    (update request :body benefactor.json/serialize)
    request))

(defn json-deserialize-response-body
  [response]
  (if (:body response)
    (update response :body benefactor.json/deserialize)
    response))
