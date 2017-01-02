(ns benefactor.cookies
  "Cookie utilities. Uses transit to serialize and deserialize values."
  (:refer-clojure :exclude [get set!])
  (:require
    [goog.net.cookies :as cookies]
    [benefactor.transit]))

(defn set!
  "Given a key and a value, sets a cookie key to the serialized value.
  Accepts an optional map of options with the following keys:
  :max-age - defaults to -1 (in seconds)
  :path - path of the cookie, defaults to the full request path
  :domain - domain of the cookie, when null the browser will use the full request host name
  :secure? - boolean specifying whether the cookie should only be sent over a secure channel"
  ([key val]
   (benefactor.cookies/set! key val nil))
  ([key val {:keys [max-age path domain secure?] :as opts}]
   (let [key (name key)
         val (benefactor.transit/serialize val)]
     (if-not opts
       (.set goog.net.cookies key val)
       (.set goog.net.cookies key val (or max-age -1) path domain (boolean secure?))))))

(defn set-secure!
  "Given a key and a value, sets a secure cookie.
  Accepts an optional map of options with the following keys:
  :max-age - defaults to -1 (in seconds)
  :path - path of the cookie, defaults to the full request path
  :domain - domain of the cookie, when null the browser will use the full request host name"
  ([key val]
   (benefactor.cookies/set! key val {:secure? true}))
  ([key val opts]
   (benefactor.cookies/set! key val (merge opts {:secure? true}))))

(defn get
  "Given a key, gets the deserialized cookie value at the key.
  Accepts an optional default when value is not found."
  ([key] (get key nil))
  ([key default]
   (or
     (some->> (name key)
              (.get goog.net.cookies)
              benefactor.transit/deserialize)
     default)))

(defn remove!
  "Given a key, removes the cookie key."
  [key]
  (.remove goog.net.cookies (name key)))

(defn clear!
  "Removes all cookies."
  []
  (.clear goog.net.cookies))
