(ns sugar.tracking
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
  [tracking]
  (if (str/blank? tracking)
    (random-base64 3)
    (str tracking "." (random-base64 3))))

(defn track
  [obj]
  (vary-meta obj update ::tracking append-tracking))

(defn get-track
  [obj]
  (::tracking (meta obj)))
