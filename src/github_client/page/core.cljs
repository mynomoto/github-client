(ns github-client.page.core
  (:require
    [clojure.string :as str]
    [github-client.config :as config]
    [github-client.db :as db]
    [github-client.page.debug :as page.debug]
    [github-client.page.entrypoint :as page.entrypoint]
    [github-client.page.exploration :as page.exploration]
    [github-client.page.login :as page.login]
    [github-client.view.layout :as layout]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn show
  [{:keys [db] :as context}]
  (let [route (cell= (:app/route (db/get-app db :github-client)))
        context (assoc context :route route)]
    (h/div :id "app"
      (layout/navbar context)
      (when-tpl config/debug?
        (s/columns
          (h/div :column 12
            (page.debug/show context))))
      (layout/container
        (s/columns
          (h/div
            :column 12
            (cond-tpl
              (cell= (or (#{:profile-edit :profile} (:domkm.silk/name route))
                         (str/blank? (:user/token (db/get-user db)))
                         (str/blank? (:user/username (db/get-user db)))))
              (page.login/show context)

              (cell= (= :index (:domkm.silk/name route)))
              (h/div
                (page.entrypoint/show context))

              (cell= (= :exploration (:domkm.silk/name route)))
              (h/div
                (page.exploration/show context))

              :else
              (h/div
                (h/h1 "Not found")))))))))
