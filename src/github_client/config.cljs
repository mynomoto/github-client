(ns github-client.config
  (:require
    [goog.object :as obj]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(goog-define test? false)
(goog-define dev? false)
(goog-define clean? false)
(goog-define last-commit "development")

(defonce debug? (cell false))

(defn debug-on!
  []
  (reset! debug? false))

(defn debug-off!
  []
  (reset! debug? false))

(obj/set js/window "debug_on" debug-on!)
(obj/set js/window "debug_off" debug-off!)
