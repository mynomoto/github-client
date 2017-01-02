(ns benefactor.util)

(defn korks->ks
  [korks]
  (if (coll? korks)
    korks
    [korks]))
