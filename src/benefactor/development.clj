(ns benefactor.development)

(defmacro when-debug [& body]
  `(when ~(vary-meta 'js/goog.DEBUG assoc :tag 'boolean)
     ~@body))
