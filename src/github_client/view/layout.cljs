(ns github-client.view.layout
  (:require
    [github-client.route :as route]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]))

(defn navbar []
  (s/navbar
    :css {:background-color "#efefef"}
    (s/navbar-section
      (s/navbar-title-link :href (route/href :index)
        "Hoplon Github Client"))
    (s/navbar-section
      (s/button-link :href "contributing.html"
        "Contributing")
      (s/button-link :href (route/href :profile)
        "Profile"))))

(defelem container
  [attr kids]
  ((h/div :class "container grid-960")
   attr
   kids))
