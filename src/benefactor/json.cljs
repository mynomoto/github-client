(ns benefactor.json
  (:require
    [benefactor.transit]
    [cognitect.transit :as t]))

(defn serialize
  "Converts a clojure object to a json string."
  [clj]
  (js/JSON.stringify (clj->js clj)))

(defn deserialize
  "Converts a json string to a clojure object tranforming keys in keywords"
  [json-str]
  (when json-str
    (js->clj (js/JSON.parse json-str) :keywordize-keys true)))

(defn serialize-t
  "Converts a clojure object to a json string. Keys need to be strings because
  this uses transit for the serialization."
  [clj]
  (benefactor.transit/serialize-verbose clj))

(defn deserialize-t
  "Converts a json string to a clojure object keeping keys as strings because
  this uses transit for the deserialization"
  [json-str]
  (when json-str
    (benefactor.transit/deserialize json-str)))
