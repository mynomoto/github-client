(ns github-client.view.layout
  (:require
    [cljs.pprint :as pprint]
    [github-client.config :as config]
    [github-client.reducer :refer [dispatch]]
    [github-client.route :as route]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]))

(defn navbar
  [{:keys [queue]}]
  (s/navbar
    :css {:background-color "#efefef"}
    (s/navbar-section
      (s/navbar-title-link
        :href (route/href :index)
        :click #(do
                  (.preventDefault %)
                  (dispatch queue [:navigate [:index]]))
        "Hoplon Github Client"))
    (s/navbar-section
      (s/button-link
        :click #(swap! config/debug? not)
        (h/text "~(if config/debug? \"Debug off \" \"Debug on\")"))
      (s/button-link
        :href "https://github.com/mynomoto/github-client"
        "Code")
      (s/button-link
        :href (route/href :profile)
        :click #(do
                  (.preventDefault %)
                  (dispatch queue [:navigate [:profile]]))
        "Profile"))))

(defelem container
  [attr kids]
  ((h/div :class "container grid-960")
   attr
   kids))

(defn flash-error
  [error url-id queue]
  (when-tpl error
    (s/columns
      (h/div :column 12
        (s/toast-error
          (s/button-clear
            :class "float-right"
            :click #(dispatch queue [:clear-flash-error [@url-id]]))
          (h/text "~(with-out-str (pprint/pprint error))"))))))
