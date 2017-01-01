(ns sugar.route
  (:require
    [domkm.silk :as silk]
    [goog.History]
    [goog.events])
  (:import
    [goog.History]))

(defn hash->path
  "Given a hash in the format \"#!/xxx\" returns the path \"/xxx\""
  [token]
  (.substr token 2))

(defn token->path
  "Given a token in the format \"!/xxx\" returns the path \"/xxx\""
  [token]
  (.substr token 1))

(defn path->hash
  "Given a path \"/xxx\" retuns a hash to be used in links \"#!/xxx\""
  [path]
  (str "#!" path))

(defn path->token
  "Given a path \"/xxx\" retuns a token to be used by .setToken \"!/xxx\""
  [path]
  (str "!" path))

(defn create
  "Given route data, create silk routes"
  [route-data]
  (silk/routes route-data))

(defn href
  "Given silk-routes, route-name and optional parameters, creates a link to the
  route-name when possible. Create a link to empty hash otherwise.  E.g.:
  (href [[:sample [[\"sample\" (silk/int :id)]]]] :sample {:id 5}) returns
  \"/sample/5\""
  ([silk-routes route-name]
   (href silk-routes route-name nil))
  ([silk-routes route-name params]
   (href silk-routes route-name params #(do
                                          (console.error ::href-failed %)
                                          (path->hash "/"))))
  ([silk-routes route-name params error-callback]
   (try
     (path->hash (silk/depart silk-routes route-name params))
     (catch js/Error _
       (error-callback [(name route-name) params])))))

(defn navigate!
  "Given a hash-router, silk-routes, route-name and optional params, navigate
  to a route when possible. Navigate to empty hash otherwise.
  E.g.:
  `(go! app-route :sample {:id 6})`"
  ([hash-router silk-routes route-name]
   (navigate! hash-router silk-routes route-name nil))
  ([hash-router silk-routes route-name params]
   (.setToken hash-router (token->path (href silk-routes route-name params)))))

(defn hash-route
  "Given a callback to be called with the event on Navigate returns a History
  instance. Docs:
  https://google.github.io/closure-library/api/goog.history.Event.html"
 [navigate-callback]
 (doto (goog.History.)
   (goog.events/listen goog.History/EventType.NAVIGATE navigate-callback)
   (.setEnabled true)))

(defn path->route
  "Given a path \"/foo\" returns the matched silk-route or nil."
  [silk-routes path]
  (silk/arrive silk-routes path))

(defn- update-route!
  "Given silk-routes, f and optional f-error if the route matches apply f to
  the parsed route, else apply f-error on the current hash"
  ([token silk-routes f]
   (update-route! token silk-routes f (fn [path] (console.error ::route-not-found path))))
  ([token silk-routes f f-error]
  (let [path (token->path token)
        parsed-route (path->route silk-routes path)]
    (if parsed-route
      (f [path parsed-route])
      (f-error path)))))

(defn navigate-event->token
  "Given a navigate event (goog.History/EventType.NAVIGATE) returns the hash
  token without the `#`. E.g.: if the route is
  \"http://exemple.com/#!/profile\" when the event is triggered it will return
  \"!/profile\""
  [event]
  (.-token event))

(defn update-route-on-hashchange!
  "Given silk-routes, f and optional f-error  add a listener to update routes.
  When the route change, if the route matches apply f to the parsed route, else
  apply f-error on the current href. "
  ([silk-routes f]
   (update-route-on-hashchange! silk-routes f (fn [hash] (console.error ::route-not-found hash))))
  ([silk-routes f f-error]
   (hash-route (fn [event] (update-route! (navigate-event->token event) silk-routes f f-error)))))
