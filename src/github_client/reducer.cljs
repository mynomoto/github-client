(ns github-client.reducer
  (:require
    [benefactor.development :as dev]
    [cljs.core.async :as async]
    [github-client.config :as config]
    [github-client.db :as db]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]])
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]]))

(defonce queue (async/chan 20))

(defn handler-not-found
  [context data]
  (console.group)
  (console.error ::handler-not-found)
  (console.log data)
  (console.groupEnd))

(defn start!
  [handler {:keys [db queue] :as context} {:keys [history selected-history]}]
  (let [current-route (cell= (:app/route (db/get-app db :github-client)))
        context (assoc context :current-route current-route)]
    (go-loop []
      (when-let [[key :as event] (async/<! queue)]
        (when-not (= :stop key)
          (try
            (dev/when-debug (console.log ::event event))
            ((key handler handler-not-found) context event)
            (db/save-history history event @db selected-history)
            (catch js/Error e
              (dev/when-debug (console.error ::handler-error e))))
          (recur))))))

(defn dispatch
  ([queue data]
   (async/put! queue (benefactor.tracking/track data)))
  ([queue data event-or-seed]
   (async/put! queue (benefactor.tracking/track data event-or-seed))))

(defn stop!
  [queue]
  (dispatch queue [:stop]))
