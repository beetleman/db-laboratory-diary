(ns db-laboratory-diary.handler-api
  (:require [compojure.core :refer [GET defroutes context]]
            [ring.middleware.defaults :refer [api-defaults
                                              wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows])
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [db-laboratory-diary.auth :as auth]
            [db-laboratory-diary.db :as db]))


(defn users [& fields]
  (select-keys (db/raw-users-all)))

(defroutes api-routes
  (context "/api" []
           (GET "/about" req (response {:name "db-laboratory-diary-api"
                                        :tables (db/tables)
                                        :current-user (auth/current-user req)
                                        :version "0.0.1"}))
           (GET "/is-auth" req (response (auth/current-user req)))
           (context "/users" []
                    (GET "/" [] (friend/authorize
                                 #{::auth/user}
                                 (response (db/user-all)))))))


(def api
  (let [handler (wrap-defaults #'api-routes api-defaults)]
    (if (env :dev)
      (-> handler (friend/authenticate
                   {:credential-fn auth/credential-fn
                    :workflows [(workflows/http-basic
                                 :realm "/")]})
          wrap-reload wrap-json-response wrap-exceptions wrap-reload)
      (-> handler (friend/authenticate {:credential-fn auth/credential-fn
                                        :workflows [(workflows/http-basic
                                                     :realm "/")]})
          wrap-reload wrap-json-response))))
