(ns benefactor.local-storage
  "Local storage utilities. Uses transit to serialize and deserialize values."
  (:refer-clojure :exclude [get set!])
  (:require
    [benefactor.transit]))

(defn set!
  "Given a key and a value sets the key to the serialized value in the local storage."
  [key val]
  (.setItem (.-localStorage js/window)
            (name key)
            (benefactor.transit/serialize val)))

(defn get
  "Given a key, gets the deserialized value in the local storage at the key.
  Accepts an optional default when value is not found."
  ([key] (get key nil))
  ([key default]
   (or (some->> (name key)
                (.getItem (.-localStorage js/window))
                benefactor.transit/deserialize)
       default)))

(defn remove!
  "Given a key, removes the local storage key."
  [key]
  (.removeItem (.-localStorage js/window)
               (name key)))

(defn clear!
  "Remove all data from local storage."
  []
  (.clear (.-localStorage js/window)))
