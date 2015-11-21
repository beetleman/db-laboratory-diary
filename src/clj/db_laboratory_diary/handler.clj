(ns db-laboratory-diary.handler
  (:require [compojure.core :refer [GET defroutes routes context]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults
                                              api-defaults
                                              wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
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
     mount-target
     (include-js "js/app.js")]]))


(defroutes site-routes
  (GET "/" [] loading-page)
  (GET "/about" [] loading-page)
  (resources "/"))

(defroutes api-routes
  (context "/api" []
           (GET "/about" [] (response {:name "db-laboratory-diary"
                                       :version "0.0.1"}))))

(def api
  (let [handler (wrap-defaults #'api-routes api-defaults)]
    (if (env :dev)
      (-> handler wrap-reload wrap-json-response wrap-exceptions wrap-reload)
      (-> handler wrap-reload wrap-json-response))))

(def site
  (let [handler (wrap-defaults #'site-routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))

(def app
  (routes
   site
   api
   (not-found "Page not fund")))
