(ns sugar.route
  (:require
    [domkm.silk :as silk]))

(defn create
  "Given route data, create silk routes"
  [route-data]
  (silk/routes route-data))

(defn href
  "Given silk-routes, route-name and optional parameters, creates a link to the
  route-name when possible. Create a link to empty hash otherwise.
  E.g.:
  `[:a {:href (href app-route :sample {:id 5})} \"Sample 5\"]`"
  ([silk-routes route-name]
   (href silk-routes route-name nil))
  ([silk-routes route-name params]
   (try
     (str "#!" (silk/depart silk-routes route-name params))
     (catch js/Error _
       (console.error :href-failed (name route-name))
       "#!/"))))

(defn navigate!
  "Given silk-routes, route-name and optional params, navigate to a route when
  possible. Navigate to empty hash otherwise.
  E.g.:
  `(go! app-route :sample {:id 6})`"
  ([silk-routes route-name]
   (navigate! silk-routes route-name nil))
  ([silk-routes route-name params]
   (set! js/window.location.hash (href silk-routes route-name params))))

(defn- update-route!
  "Given silk-routes, f and optional f-error if the route matches apply f to
  the parsed route, else apply f-error on the current href. "
  ([silk-routes f]
   (update-route! silk-routes f (fn [_])))
  ([silk-routes f f-error]
  (let [hash (some-> js/window.location.hash (.substr 2))
        parsed-route (silk/arrive silk-routes hash)]
    (if parsed-route
      (f (dissoc parsed-route :domkm.silk/routes :domkm.silk/url))
      (f-error js/window.location.hash)))))

(defn update-route-on-hashchange!
  "Given silk-routes, f and optional f-error  add a listener to update routes.
  When the route change, if the route matches apply f to the parsed route, else
  apply f-error on the current href. "
  ([silk-routes f]
   (update-route-on-hashchange! silk-routes f (fn [_])))
  ([silk-routes f f-error]
   (-> js/window .-onhashchange
       (set! (fn [_] (update-route! silk-routes f f-error))))
   (update-route! silk-routes f (fn [_] (navigate! silk-routes :index)))))
