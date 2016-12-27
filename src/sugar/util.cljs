(ns sugar.util)

(defn korks->ks
  [korks]
  (if (coll? korks)
    korks
    [korks]))
