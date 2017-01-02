(ns benefactor.keycodes)

(def to-code
  {:enter 13})

(defn event->code
  [event]
  (.-charCode event))
