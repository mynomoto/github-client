(ns github-client.config
  (:require
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(goog-define clean? false)
(goog-define last-commit "development")

(defonce debug? (cell false))

(defn debug-on!
  []
  (reset! debug? false))

(defn debug-off!
  []
  (reset! debug? false))
