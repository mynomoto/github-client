(ns github-client.page.entrypoint
  (:require
    [clojure.string :as str]
    [github-client.db :as db]
    [github-client.api :as api]
    [github-client.reducer :refer [dispatch]]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [benefactor.local-storage]
    [benefactor.keycodes]))

(def key->route
  {:rate_limit_url [:rate-limit {:query-params {:display "show"}}]})

(defn show
  [{:keys [route db queue]}]
  (let [app (cell= (db/get-app db :github-client))
        urls (cell= (sort-by first (:app/url app)))]
    (h/div
      (h/h3 "Api Entrypoint")
      (s/table :options #{:striped :hover}
        (h/thead
          (h/tr
            (h/th "Key")
            (h/th "Url")))
        (h/tbody
          (for-tpl [[key _] urls]
                   (h/tr
                     (h/td (h/text "~{key}"))
                     (h/td (s/button-primary
                             :click #(dispatch queue [:navigate (or (key->route @key) [:exploration {:url-id (name @key) :query-params {:display "raw"}}])])
                             "Explore")))))))))
