(ns javelin.datascript
  (:require
    [datascript.core :as datascript]
    [javelin.core :as j]))

(defn create-conn [& [schema]]
  (j/cell (datascript/empty-db schema)
    :meta {:listeners (j/cell {})}))
