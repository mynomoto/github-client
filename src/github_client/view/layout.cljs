(ns github-client.view.layout
  (:require
    [cljs.pprint :as pprint]
    [github-client.config :as config]
    [github-client.reducer :refer [dispatch]]
    [github-client.route :as route]
    [hoplon.core :as h :refer [defelem case-tpl cond-tpl for-tpl if-tpl when-tpl]]
    [hoplon.spectre-css :as s]
    [javelin.core :as j :refer [cell] :refer-macros [cell= defc defc=]]))

(defn navbar
  [{:keys [queue]}]
  (s/navbar
    (s/navbar-section
      (s/navbar-title-link
        :css {:font-size "2rem"
              :font-weight "bold"
              :padding-right "2rem"
              :text-decoration "none"
              :vertical-align "middle"}
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

(defn page-link
  ([t click active?]
   (page-link t click active? true false))
  ([t click active? show?]
   (page-link t click active? show? false))
  ([t click active? show? disabled?]
   (when-tpl show?
     (let [attributes {:class (cell= {:disabled disabled?})
                       :options (cell= (if active? #{:active} #{}))
                       :href ""}]
       (s/page-item
         (assoc attributes :click (fn [event]
                                    (.preventDefault event)
                                    (if click
                                      (click))))
         (h/text "~{t}"))))))

(defn pagination
  ([page last-page per-page queue search route]
   (pagination page last-page per-page queue search route nil))
  ([page last-page per-page queue search route params]
   (let [hfn (fn [f page]
               #(let [go-to-page (f @page)]
                  (dispatch queue [:navigate [route (merge-with merge {:query-params {:display "show"
                                                                                      :search @search
                                                                                      :page go-to-page
                                                                                      :per-page @per-page}} params)]])))]
     (s/pagination
       (page-link "Previous" (hfn #(max 1 (dec %)) page) false true (cell= (= 1 page)))
       (page-link "1" (hfn (fn [x] 1) page) false (cell= (not= page 1)))
       (page-link "…" nil false (cell= (> (- page 3) 1)) true)
       (page-link (cell= (- page 2)) (hfn #(- % 2) page) false (cell= (> (- page 2) 1)))
       (page-link (cell= (dec page)) (hfn #(dec %) page) false (cell= (> (dec page) 1)))
       (page-link page nil true)
       (page-link (cell= (inc page)) (hfn #(inc %) page) false (cell= (< (inc page) last-page)))
       (page-link (cell= (+ page 2)) (hfn #(+ % 2) page) false (cell= (< (+ page 2) last-page)))
       (page-link "…" nil false (cell= (< (+ page 3) last-page)) true)
       (page-link last-page (hfn identity last-page) false (cell= (and (< page last-page) (not= 1 last-page))))
       (page-link "Next" (hfn #(min @last-page (inc %)) page) false true
         (cell= (>= page last-page)))))))

