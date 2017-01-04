(task-options!
  pom {:project     'github-client
       :version     "0.0.1"
       :description "A github client built with Hoplon"
       :license     {"EPL" "http://www.eclipse.org/legal/epl-v10.html"}})

(set-env!
  :dependencies '[[org.clojure/clojure "1.9.0-alpha14" :scope "compile"]

                  ;; Clojurescript
                  [adzerk/boot-cljs "1.7.228-2" :scope "compile"]
                  [org.clojure/clojurescript "1.9.293" :scope "compile"]

                  ;; Clojurescript repl
                  [adzerk/boot-cljs-repl "0.3.3" :scope "compile"]
                  [com.cemerick/piggieback "0.2.1" :scope "compile"]
                  [weasel "0.7.0" :scope "compile"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "compile"]

                  ;; Clojurescript test
                  [crisptrutski/boot-cljs-test "0.3.0" :scope "compile"]
                  [juxt/iota "0.2.3" :scope "compile"]

                  ;; Auto reload
                  [adzerk/boot-reload "0.5.0" :scope "compile"]

                  ;; Serve static contents
                  [tailrecursion/boot-static "0.1.0" :scope "compile"]

                  ;; Hoplon
                  [hoplon "6.0.0-alpha17" :scope "compile"]
                  [hoplon/javelin "3.9.0" :scope "compile"]
                  [mynomoto/hoplon-spectre.css "0.1.0" :scope "compile"]

                  [benefactor "0.0.1-SNAPSHOT"]
                  ;; Better devtools for Clojurescript
                  [binaryage/devtools "0.8.3" :scope "compile"]
                  [powerlaces/boot-cljs-devtools "0.1.3-SNAPSHOT" :scope "compile"]

                  ;; Create static site and upload to S3
                  [confetti "0.1.4" :scope "compile"]

                  ;; Gzip all things
                  [org.martinklepsch/boot-gzip "0.1.3" :scope "compile"]

                  ;; Database
                  [datascript "0.15.5"]
                  [datascript-transit "0.2.2"]

                  ;; i18n
                  [com.taoensso/tower "3.1.0-beta5"] ;; not sure about this

                  ;; http
                  [funcool/httpurr "0.6.2"]
                  [funcool/promesa "1.7.0"]

                  ;; routes
                  [com.domkm/silk "0.1.2"]

                  ;; util functions
                  [medley "0.8.4"]

                  ;; channels
                  [org.clojure/core.async "0.2.395"]]
  :source-paths #{"src"}
  :asset-paths #{"assets"})

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload :refer [reload]]
  '[boot.git :refer [clean? last-commit]]
  '[clojure.java.io :as io]
  '[clojure.string :as str]
  '[confetti.boot-confetti :refer [sync-bucket create-site]]
  '[crisptrutski.boot-cljs-test :refer [test-cljs]]
  '[org.martinklepsch.boot-gzip :refer [gzip]]
  '[tailrecursion.boot-static :refer [serve]]
  '[powerlaces.boot-cljs-devtools :refer [cljs-devtools]])

(defn- last-commit*
  []
  (try (subs (last-commit) 0 8) (catch Throwable _)))

(defn- clean?*
  []
  (try (clean?) (catch Throwable _)))

(deftask hoplon-cards []
  (merge-env! :resource-paths #{"hoplon_cards"})
  identity)

(deftask dev
  []
  "Build github-clieent for development."
  (comp
    (hoplon-cards)
    (watch)
    (speak)
    (cljs-devtools)
    (cljs-repl) ; do not change the order!
    (reload :on-jsload 'github-client.reload/on-js-reload)
    (cljs
      :optimizations :none
      :compiler-options {:parallel-build true
                         :closure-defines {'github-client.config/dev? true
                                           'github-client.config/clean? (clean?*)
                                           'github-client.config/last-commit (last-commit*)}})
    (serve :port 8000)))

(deftask prod
  "Build github-client for production deployment."
  []
  (comp
    (cljs
      :optimizations :advanced
      :compiler-options {:parallel-build true
                         :language-in  :ecmascript5
                         :closure-defines {'github-client.config/clean? (clean?*)
                                           'github-client.config/last-commit (last-commit*)}})
    (sift :include #{#"\.out"} :invert true)
    (target :dir #{"target"})))

(deftask testing []
  (merge-env! :source-paths #{"test"})
  identity)

(deftask autotest
  []
  (comp
    (testing)
    (watch)
    (speak)
    (test-cljs :cljs-opts {:closure-defines {'github-client.config/test? true}})))

(deftask run-tests
  []
  (comp
    (testing)
    (test-cljs :exit? true
      :cljs-opts {:closure-defines {'github-client.config/test? true}})))

(def file-maps-file "file-maps.edn")

(defn ->s3-key
  [fileset-path]
  (let [p (str/replace fileset-path #"\.gz$" "")]
    p))

(defn fileset->file-maps [fileset]
  (for [[_ tmpf] (:tree fileset)
        :let [gzip? (.endsWith (:path tmpf) ".gz")]]
    {:s3-key   (->s3-key (:path tmpf))
     :file     (.getCanonicalPath (tmp-file tmpf))
     :metadata (when gzip? {:content-encoding "gzip"})}))

(deftask save-file-maps []
  (with-pre-wrap fs
    (let [tmp (tmp-dir!)]
      (spit (io/file tmp file-maps-file)
            (pr-str (fileset->file-maps fs)))
      (-> fs (add-resource tmp) commit!))))

(deftask deploy
  []
  (comp
    (cljs :optimizations :advanced)
    (sift :include [#"main\.out/.*" #".*\.cljs"]
      :invert true)
    (gzip :regex [#".*\.css$" #".*\.js$" #".*\.html$"])
    (sift :include [#".*\.css$" #".*\.js$" #".*\.html$"]
      :invert true)
    (save-file-maps)
    (sync-bucket :file-maps-path file-maps-file
      :bucket "s3-bucket-name"
      :prune true
      :access-key (System/getenv "AWS_ACCESS_KEY_ID")
      :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY"))))
