(ns github-client.config
  (:require
    [goog.object :as obj]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(goog-define test? false)
(goog-define dev? false)

(defc debug true)

(defn debug!
  []
  (reset! debug true))

(defn not-debug!
  []
  (reset! debug false))

(obj/set js/window "activate_debug" debug!)
(obj/set js/window "deactivate_debug" not-debug!)
