(ns sugar.transit
  (:require
    [cognitect.transit :as t]))

(def writer (t/writer :json))
(def verbose-writer (t/writer :json-verbose))

(def reader (t/reader :json))

(defn serialize
  "Serialize data to transit with the default transit writer"
  [data]
  (t/write writer data))

(defn serialize-verbose
  "Serialize data to transit with the default transit writer"
  [data]
  (t/write verbose-writer data))

(defn deserialize
  "Deserialize data from transit with the default transit reader"
  [str]
  (t/read reader str))
