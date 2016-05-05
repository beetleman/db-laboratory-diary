(ns db-laboratory-diary.handler-api
  (:require [compojure.core :refer [GET POST  DELETE defroutes context routes]]
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


(defn area_data []
  (routes
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
                      :name name}))))))


(defn experiments []
  (routes
   (GET "/" []
        (friend/authorize
         #{::auth/user}
         (response (db/raw-experiments-with-area_data-all))))
   (POST "/" [manager_id laborants_ids area_data_id fertilizer
              start_date stop_date]
         (friend/authorize
          #{::auth/admin}
          (response (db/experiments-create<!
                     {:manager_id manager_id
                      :laborants_ids laborants_ids
                      :area_data_id area_data_id
                      :fertilizer fertilizer
                      :start_date start_date
                      :stop_date stop_date}))))
   (context "/:experiment_id" [experiment_id]
            (GET "/" []
                 (friend/authorize
                  #{::auth/user}
                  (response (db/experiments-get
                             {:id experiment_id}))))
            (POST "/laborants" [laborant_id]
                  (friend/authorize
                   #{::auth/admin}
                   (response (db/add-laborant-to-experiment<!
                              {:experiment_id experiment_id
                               :laborant_id laborant_id}))))
            (DELETE "/laborants" [laborant_id]
                    (friend/authorize
                     #{::auth/admin}
                     (response (db/delete-laborant-from-experiment!
                                {:experiment_id experiment_id
                                 :laborant_id laborant_id}))))
            (POST "/surfaces" [area]
                  (friend/authorize
                   #{::auth/admin}
                   (response (db/add-laborant-to-experiment<!
                              {:experiment_id experiment_id
                               :area area}))))
            (GET "/mesurments" []
                 (friend/authorize
                  #{::auth/user}
                  (response (db/all-mesurments-for-experiment
                             {:experiment_id experiment_id}))))
            (POST "/mesurments" req
                  (friend/authorize
                   #{::auth/user}
                   (response (let [mesurments (map (fn [[id success]]
                                                     {:surface_id id :success success})
                                                   (:form-params req))]
                               (db/add-mesurments-to-expetiment<! mesurments))))))))


(defn users []
  (routes
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
                      :is_admin (= is_admin "true")}))))))


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
                    (area_data))
           (context "/experiments" []
                    (experiments))
           (GET "/my_experiments" req
                (friend/authorize
                 #{::auth/user}
                 (response (db/all-experiments-for-user-all
                            {:user_id (-> req
                                          (auth/current-user)
                                          :id)}))))
           (context "/users" []
                    (users))))


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
