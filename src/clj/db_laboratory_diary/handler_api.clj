(ns db-laboratory-diary.handler-api
  (:require [compojure.core :refer [GET POST defroutes context]]
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
           (POST "/check-credencials" [username password]
                 (response (auth/check-user-password username password)))
           (GET "/is-auth" req (response (auth/current-user req)))
           (context "/area_data" []
                    (GET "/" []
                         (friend/authorize
                          #{::auth/admin}
                          (response (db/raw-area_data-all))))
                    (POST "/" [max_area address name]
                          (friend/authorize
                           #{::auth/admin}
                           (response (db/area_data-create<!
                                      {:max_area max_area
                                       :address address
                                       :name name})))))
           (context "/experiments" []
                    (GET "/" []
                         (friend/authorize
                          #{::auth/admin}
                          (response (db/raw-experiments-all))))
                    (POST "/" [manager_id area_data_id fertilizer
                               start_date stop_date]
                         (friend/authorize
                          #{::auth/admin}
                          (response (db/experiments-create<!
                                     {:manager_id manager_id
                                      :area_data_id area_data_id
                                      :fertilizer fertilizer
                                      :start_date start_date
                                      :stop_date stop_date}))))
                    (context "/:experiment_id" [experiment_id]
                             (GET "/" []
                                  (response (db/experiments-get
                                             {:experiment_id experiment_id})))))
           (context "/users" []
                    (GET "/" []
                         (friend/authorize
                          #{::auth/admin}
                          (response (db/user-all true))))
                    (POST "/" [username is_admin email]
                          (friend/authorize
                           #{::auth/admin}
                           (response (db/users-create<!
                                      {:username username
                                       :email email
                                       :is_admin (= is_admin "true")})))))))


(def api
  (let [handler (wrap-defaults #'api-routes api-defaults)]
    (if (env :dev)
      (-> handler (friend/authenticate
                   {:credential-fn auth/credential-fn
                    :workflows [(workflows/http-basic
                                 :realm "/")]})
          wrap-json-response wrap-exceptions wrap-reload)
      (-> handler (friend/authenticate {:credential-fn auth/credential-fn
                                        :workflows [(workflows/http-basic
                                                     :realm "/")]})
          wrap-json-response))))
