(ns javelin.datascript
  (:refer-clojure :exclude [select-keys])
  (:require
    [datascript.core :as datascript]
    [javelin.core :as j]
    [sugar.util]))

(def empty-db datascript/empty-db)

(defn create-conn [& [schema]]
  (j/cell (empty-db schema)
    :meta {:listeners (j/cell {})}))

(def transact! datascript/transact!)

(def entity datascript/entity)

(def q datascript/q)

(defn touch
  "A nil safe touch because the entity may be nil sometimes."
  [entity]
  (when entity
    (datascript/touch entity)))

(defn select-keys
  "A nil safe select keys for use with entities so it don't try to fetch a nil key."
  [entity korks]
  (->> (sugar.util/korks->ks korks)
       (remove nil?)
       (clojure.core/select-keys entity)))
