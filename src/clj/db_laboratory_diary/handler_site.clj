(ns db-laboratory-diary.handler-site
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults
                                              wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]))

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])


(def github-ribbon
  [:a {:href "https://github.com/beetleman/db-laboratory-diary"}
   [:img {:style "position: absolute; top: 0; right: 0; border: 0;"
          :src "https://camo.githubusercontent.com/e7bbb0521b397edbd5fe43e7f760759336b5e05f/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f677265656e5f3030373230302e706e67"
          :alt "Fork me on GitHub"
          :data-canonical-src "https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png"}]])

(def loading-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (if (:dev env)
       [:title "evn: DEV"])
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     github-ribbon
     mount-target
     (include-js "js/app.js")]]))

(defroutes site-routes
  (GET "/" [] loading-page)
  (GET "/about" [] loading-page)
  (resources "/"))

(def site
  (let [handler (wrap-defaults #'site-routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
