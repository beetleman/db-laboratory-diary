(ns db-laboratory-diary.handler-api
  (:require [compojure.core :refer [GET defroutes context]]
            [ring.middleware.defaults :refer [api-defaults
                                              wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [db-laboratory-diary.db :as db]))


(defroutes api-routes
  (context "/api" []
           (GET "/about" [] (response {:name "db-laboratory-diary-api"
                                       :tables (db/tables)
                                       :version "0.0.1"}))))

(def api
  (let [handler (wrap-defaults #'api-routes api-defaults)]
    (if (env :dev)
      (-> handler wrap-reload wrap-json-response wrap-exceptions wrap-reload)
      (-> handler wrap-reload wrap-json-response))))
