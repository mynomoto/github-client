(ns benefactor.tracking
  (:refer-clojure :exclude [get])
  (:require
    [clojure.string :as str]))

(def base64-characteres
  "List of valid base 64 characteres."
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/")

(defn random-base64
  "Generate a random string of base 64 characteres with n characteres."
  [n]
  (str/join (repeatedly n #(rand-nth base64-characteres))))

(defn append-tracking
  ([] (random-base64 3))
  ([tracking]
   (if (str/blank? tracking)
     (random-base64 3)
     (str tracking "." (random-base64 3)))))

(defn get-track
  [obj]
  (::tracking (meta obj)))

(defn get-timestamp
  [obj]
  (::timestamp (meta obj)))

(defn track
  ([obj]
   (vary-meta obj assoc
     ::tracking (append-tracking)
     ::timestamp (.toISOString (js/Date.))))
  ([obj origin]
   (if (string? origin)
     (vary-meta obj assoc
       ::tracking (append-tracking origin)
       ::timestamp (.toISOString (js/Date.)))
     (let [tracking (get-track origin)]
       (vary-meta obj assoc
         ::tracking (append-tracking tracking)
         ::timestamp (.toISOString (js/Date.)))))))
