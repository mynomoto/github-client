(ns github-client.reload)

(defn on-js-reload []
  (if (.startsWith js/window.location.pathname "/cards.html")
    (js/cards_js_reload)
    (js/core_js_reload)))
