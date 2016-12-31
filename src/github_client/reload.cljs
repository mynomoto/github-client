(ns github-client.reload)

(defn on-js-reload []
  (if (.startsWith js/window.location.pathname "/cards.html")
    (js/cards.core.reload)
    (js/github_client.core.reload)))
