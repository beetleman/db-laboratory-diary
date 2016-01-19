(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery defqueries]]
            [environ.core :refer [env]]
            [clj-time.core :as timec]
            [clj-time.format :as timef]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(def db (env :db-url))

(defquery tables "db/tables.sql" {:connection db})

;; UTILS
(def error-message
  "deffault error mesage"
  "Wrong data!")

(defn get-error-message [message]
  {:data nil :error message})

(defn get-success-message [data]
  {:data data :error nil})

(defmacro defquery-with-message
  ([query-name query]
   `(defquery-with-message ~query-name ~query error-message))
  ([query-name query error-message]
   `(defn ~query-name [args#]
      (try
        (get-success-message (~query args#))
        (catch Exception e# (get-error-message ~error-message))))))

;; USERS

(def user-fields-public [:id :username :lastname :firstname])
(def user-fields-priv [:id :username :lastname :firstname :is_admin :email])

(defqueries "db/users.sql" {:connection db})

(defn hash-password
  "hash passwprd using bcrypt"
  [to-hash]
  (creds/hash-bcrypt) to-hash)

(def default-admin-user
  {:name "admin"
   :firstname nil
   :lastname nil
   :username "admin"
   :email "admin@fake.pl"
   :password "qwerty1"
   :is_admin true})

(defn user-count []
  (-> (raw-users-count) first :count))

(defn users-create<!
  "create new user with hashing password"
  [user]
  (let [user (merge
              {:firstname nil
               :lastname nil
               :password "password"}
              user)]
    (try
      (get-success-message (select-keys
                        (raw-users-create<! (update user :password hash-password))
                        user-fields-priv))
      (catch Exception e (get-error-message "Username or password exists in db!")))))

(defn user-all
  ([]
   (user-all false))
  ([admin?]
   (let [fields (if admin?
                  user-fields-priv
                  user-fields-public)]
     (map (fn [user] (select-keys user fields))
          (raw-users-all)))))

(defn users-save!
  "update user with hashing password"
  [id user]
  (let [old-user (raw-users-get {:id id})
        new-user (merge old-user user)
        password (if (:password user)
                   (hash-password (:password user))
                   (:password old-user))]
    (raw-users-save! (assoc new-user :password password))))

(defn default-admin-create<!
  "if users table is empty create default admin with:
  `username': `admin'
  `passord': `qwerty1'"
  []
  (if (zero? (user-count))
    (users-create<! default-admin-user)))

;; AREA_DATA

(defqueries "db/area_data.sql" {:connection db})
(defquery-with-message area_data-create<! raw-area_data-create<!)


;; SURFACES

(defqueries "db/surfaces.sql" {:connection db})


;; EXPERIMENTS

(defqueries "db/experiments.sql" {:connection db})

(defn experiments-create<! [experiment]
  (let [start_date (-> experiment :start_date (timef/parse))
        stop_date (-> experiment :stop_date (timef/parse))
        experiment (merge experiment {:start_date start_date
                                      :stop_date stop_date})]
    (if (and stop_date start_date (timec/after? stop_date start_date))
      (try
        (get-success-message (raw-experiments-create<! experiment))
        (catch Exception e (get-error-message "Errors in referencces!")))
      (get-error-message "Errors in 'start date or 'stop date'!"))))


(defn experiments-get [args]
  (if-let [experiment (raw-experiments-get args)]
    (merge experiment
           {:manager (first (raw-users-get
                      {:id (:manager experiment)}))
            :area_data (first (raw-area_data-get
                               {:id (:area_data experiment)}))
            :surfaces (raw-surfaces-get-by-experiment args)
            :laborants (raw-all-laborant-for-experiment args)})))


(defquery-with-message
  add-surface-to-experiment<!
  raw-add-surface-to-experiment<!
  "Bad surface area or experiment dont exist")

(defquery-with-message
  add-laborant-to-experiment<!
  raw-add-laborant-to-experiment<!
  "Bad user id or experiment dont exist")

(defquery-with-message
  delete-laborant-from-experiment!
  raw-delete-laborant-from-experiment!
  "Laborant or experiment dont exist")

(defquery-with-message
  add-mesurment-to-surfaces<!
  raw-add-mesurment-to-surfaces<!
  "Surface dont exist or or duble mesurment")

(defquery-with-message
  all-mesurments-for-surfaces
  raw-all-mesurments-for-surfaces
  "Surface dont exist")
