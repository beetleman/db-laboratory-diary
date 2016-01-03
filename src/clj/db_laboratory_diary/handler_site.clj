(ns db-laboratory-diary.handler-site
  (:require [compojure.core :refer [GET ANY POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults
                                              wrap-defaults]]
            [hiccup.core :refer [html]]
            [cemerick.friend :as friend]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]))

(def mount-target
  [:div#app {:class "container"}
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])


(def github-ribbon
  [:a {:class "github-ribbon"
       :href "https://github.com/beetleman/db-laboratory-diary"}
   [:img {:src "https://camo.githubusercontent.com/e7bbb0521b397edbd5fe43e7f760759336b5e05f/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f677265656e5f3030373230302e706e67"
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
     [:link {:rel "stylesheet"
             :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
             :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
             :crossorigin "anonymous"}]
     [:link {:rel "stylesheet"
             :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"
             :integrity "sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r"
             :crossorigin "anonymous"}]
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     github-ribbon
     mount-target
     (include-js "js/app.js")]]))

(defroutes site-routes
  (GET "/" [] loading-page)
  (GET "/about" [] loading-page)
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (resources "/"))

(def site
  (let [handler (wrap-defaults #'site-routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
