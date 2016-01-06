(ns db-laboratory-diary.handler
  (:require [compojure.core :refer [routes]]
            [compojure.route :refer [not-found]]
            [db-laboratory-diary.handler-site :refer [site]]
            [db-laboratory-diary.handler-api :refer [api]]))

(def app
  (routes
   api
   site
   (not-found "Page not fund:/")))
