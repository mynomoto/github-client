(ns github-client.reducer
  (:require
    [cljs.core.async :as async]
    [github-client.db :as db]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]])
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]]))

(defonce queue (async/chan 20))

(defn handler-not-found
  [context data]
  (console.log (str (:key context) " doesn't have a handler"))
  (console.log :data data))

(defn start!
  [handler db queue]
  (let [context {:db db :queue queue}
        route (cell= (:app/route (db/get-app db :github-client)))]
    (go-loop []
      (when-let [[key data :as event] (async/<! queue)]
        (when-not (= :stop key)
          (console.log :event event)
          ((key handler handler-not-found) (assoc context :key key :route route) data)
          (recur))))))

(defn dispatch
  [queue data]
  (async/put! queue data))

(defn stop!
  [queue]
  (dispatch queue [:stop]))

