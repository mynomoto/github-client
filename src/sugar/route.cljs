(ns sugar.route
  (:require
    [domkm.silk :as silk]
    [goog.History]
    [goog.events])
  (:import
    [goog.History]))

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
     (str "!" (silk/depart silk-routes route-name params))
     (catch js/Error _
       (console.error ::href-failed (name route-name))
       "!/"))))

(defn navigate!
  "Given silk-routes, route-name and optional params, navigate to a route when
  possible. Navigate to empty hash otherwise.
  E.g.:
  `(go! app-route :sample {:id 6})`"
  ([hash-router silk-routes route-name]
   (navigate! hash-router silk-routes route-name nil))
  ([hash-router silk-routes route-name params]
   (.setToken hash-router (href silk-routes route-name params))))

(defn hash-route
  "Given a callback to be called with the event on Navigate returns a History
  instance. Docs:
  https://google.github.io/closure-library/api/goog.history.Event.html"
 [navigate-callback]
 (doto (goog.History.)
   (goog.events/listen goog.History/EventType.NAVIGATE navigate-callback)
   (.setEnabled true)))

(defn- update-route!
  "Given silk-routes, f and optional f-error if the route matches apply f to
  the parsed route, else apply f-error on the current hash"
  ([event silk-routes f]
   (update-route! silk-routes f (fn [hash] (console.error :route-not-found hash))))
  ([event silk-routes f f-error]
  (let [hash (some-> event .-token (.substr 2))
        parsed-route (silk/arrive silk-routes hash)]
    (if parsed-route
      (f (dissoc parsed-route :domkm.silk/routes :domkm.silk/url))
      (f-error hash)))))

(defn update-route-on-hashchange!
  "Given silk-routes, f and optional f-error  add a listener to update routes.
  When the route change, if the route matches apply f to the parsed route, else
  apply f-error on the current href. "
  ([silk-routes f]
   (update-route-on-hashchange! silk-routes f (fn [hash] (console.error :route-not-found hash))))
  ([silk-routes f f-error]
   (hash-route (fn [event] (update-route! event silk-routes f f-error)))))
