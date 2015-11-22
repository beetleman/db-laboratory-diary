(defproject db-laboratory-diary "0.1.0-SNAPSHOT"
  :description "Study project"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring-server "0.4.0"]
                 [reagent "0.5.1"]
                 [reagent-forms "0.5.13"]
                 [reagent-utils "0.1.5"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [yesql "0.5.1"]
                 [migratus "0.8.4"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [environ "1.0.1"]
                 [org.clojure/clojurescript "1.7.170" :scope "provided"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.4"]]

  :plugins [[lein-environ "1.0.1"]
            [migratus-lein "0.1.7"]
            [lein-cljsbuild "1.1.1"]
            [lein-asset-minifier "0.2.2"]]

  :migratus {:store :database
             :migration-dir "db/migrations/"
             :db {:classname "org.sqlite.JDBC"
                  :subprotocol "sqlite"
                  :subname "sqlite.db"}}

  :ring {:handler db-laboratory-diary.handler/app
         :uberwar-name "db-laboratory-diary.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "db-laboratory-diary.jar"

  :main db-laboratory-diary.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "src/cljc"]
                             :compiler {:output-to "target/cljsbuild/public/js/app.js"
                                        :output-dir "target/cljsbuild/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}
  :less {:source-paths ["src/less"]
         :target-path "resources/public/css"}

  :profiles {:dev {:repl-options {:init-ns db-laboratory-diary.repl}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.4.0"]
                                  [lein-figwheel "0.5.0-SNAPSHOT"]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [com.cemerick/piggieback "0.1.5"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.0-SNAPSHOT"]
                             [cider/cider-nrepl "0.10.0-SNAPSHOT"]
                             [refactor-nrepl "2.0.0-SNAPSHOT"]
                             [lein-less "1.7.5"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 7002
                              :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                                                 "cider.nrepl/cider-middleware"
                                                 "refactor-nrepl.middleware/wrap-refactor"
                                                 ]
                              :css-dirs ["resources/public/css"]
                              :ring-handler db-laboratory-diary.handler/app}

                   :env {:dev true
                         :db {:classname "org.sqlite.JDBC"
                              :subprotocol "sqlite"
                              :subname "sqlite.db"}}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "db-laboratory-diary.dev"
                                                         :source-map true}}
                                        }}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :prep-tasks ["compile" ["cljsbuild" "once"]]
                       :env {:production true
                             :db {:classname "org.sqlite.JDBC"
                                  :subprotocol "sqlite"
                                  :subname "sqlite.db"}}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
