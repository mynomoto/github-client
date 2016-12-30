(ns github-client.page.login
  (:require
    [clojure.string :as str]
    [sugar.datascript.form :as form]
    [github-client.db :as db]
    [github-client.reducer :refer [dispatch]]
    [github-client.route :as route]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]
    [sugar.keycodes]
    [sugar.local-storage]))

(defn error-label
  [show? text]
  (h/p
    :css {:color "#e85600"}
    :display show?
    (h/text "~{text}")))

(defn show
  [{:keys [route db queue]}]
  (let [user (cell= (db/get-user db))
        username (cell= (:user/username user))
        token (cell= (:user/token (db/get-user db)))
        login? (cell= (or (str/blank? (:user/token user))
                          (str/blank? (:user/username user))))
        edit? (cell= (= :profile-edit (:domkm.silk/name route)))]
    (h/div
      (h/h3 "Github Client")
      (h/p "You can generate a Github personal access token at "
        (h/a :href "https://github.com/settings/tokens" :target "_blank" "Personal access token") ".")
      (h/p "Github API docs can be found at "
        (h/a :href "https://developer.github.com/v3/" :target "_blank" "Github Developer") ".")
      (s/card
        (s/card-header
          (h/h4 :class "card-title"
            "Login"))
        (s/card-body
          (h/form
            (s/form-group
              :options (cell= (if (form/error db ::login :user/username) #{:error} #{}))
              (s/form-label "Github username")
              (h/p
                :toggle (cell= (and (not edit?) (not login?)))
                (h/text "~(:user/username user)"))
              (s/input
                :toggle (cell= (or edit? login?))
                :css {:width "350px"}
                :type "text"
                :value (cell= (or (form/value db ::login :user/username)
                                  (:user/username user)))
                :change (fn [e]
                          (dispatch queue [:set-form-field [::login :user/username @e]]))
                :keypress (fn [e]
                            (when (= (sugar.keycodes/to-code :enter)
                                     (sugar.keycodes/event->code e))
                              (dispatch queue [:set-form-field [::login :user/username @e]])
                              (dispatch queue [:update-profile])))
                :placeholder "Username")
              (error-label
                (cell= (and edit?
                            (form/error db ::login :user/username)
                            (form/dirty? db ::login :user/username)))
                (cell= (form/error db ::login :user/username))))
            (s/form-group
              :options (cell= (if (form/error db ::login :user/token) #{:error} #{}))
              (s/form-label "Personal access token")
              (h/p
                :toggle (cell= (and (not edit?) (not login?)))
                (h/text "~{token}"))
              (s/input
                :toggle (cell= (or edit? login?))
                :css {:width "350px"}
                :type "text"
                :value (cell= (or (form/value db ::login :user/token)
                                  (:user/token user)))
                :change (fn [e]
                          (dispatch queue [:set-form-field [::login :user/token @e]]))
                :keypress (fn [e]
                            (when (= (sugar.keycodes/to-code :enter)
                                     (sugar.keycodes/event->code e))
                              (dispatch queue [:set-form-field [::login :user/token @e]])
                              (dispatch queue [:update-profile])))
                :placeholder "Token")
              (error-label
                (cell= (and edit?
                            (form/error db ::login :user/token)
                            (form/dirty? db ::login :user/token)))
                (cell= (form/error db ::login :user/token))))
            (s/button-primary
              :toggle login?
              :click (fn [_]
                       (dispatch queue [:login-submit]))
              "Sign in")
            (s/button-primary
              :toggle (cell= (and (#{:profile} (:domkm.silk/name route))
                                  (not login?)))
              :click (fn [_]
                       (dispatch queue [:navigate [:profile-edit]]))
              "Edit")
            (s/button-primary
              :toggle (cell= (and (#{:profile-edit} (:domkm.silk/name route))
                                  (not login?)))
              :click (fn [_]
                       (dispatch queue [:login-submit]))
              "Save")
            (s/button
              :toggle (cell= (and (#{:profile-edit} (:domkm.silk/name route))
                                  (not login?)))
              :click (fn [_]
                       (dispatch queue [:clear-form-values ::login])
                       (dispatch queue [:navigate [:profile]]))
              "Cancel")))))))
