(ns github-client.reducer
  (:require
    [cljs.core.async :as async])
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]]))

(defonce queue (async/chan 20))

(defn handler-not-found
  [context data]
  (console.log (str (:key context) " doesn't have a handler"))
  (console.log :data data))

(defn start!
  [handler db queue]
  (let [context {:db db :queue queue}]
    (go-loop []
      (when-let [[key data] (async/<! queue)]
        (when-not (= :stop key)
          (console.log key data)
          ((key handler handler-not-found) (assoc context :key key) data)
          (recur))))))

(defn dispatch
  [queue data]
  (async/put! queue data))

(defn stop!
  [queue]
  (dispatch queue [:stop]))

