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
(defn error-message [message]
  {:data nil :error message})

(defn success-message [data]
  {:data data :error nil})


;; USERS

(def user-fields-public [:id :username :lastname :firstname])
(def user-fields-priv [:id :username :lastname :firstname :is_admin :email])

(defqueries "db/users.sql" {:connection db})

(defn hash-password
  "hash passwprd using sha1"
  [to-hash]
  (creds/hash-bcrypt to-hash))

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
      (success-message (select-keys
                        (raw-users-create<! (update user :password hash-password))
                        user-fields-priv))
      (catch Exception e (error-message "Username or password exists in db!")))))

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


;; EXPERIMENTS

(defqueries "db/experiments.sql" {:connection db})

(defn raw-experiments-create<! [experiment]
  (let [start_date (-> experiment :start_date (timef/parse))
        stop_date (-> experiment :stop_date (timef/parse))
        experiment (merge experiment {:start_date start_date
                                      :stop_date stop_date})]
    (if (and stop_date start_date (timec/after? stop_date start_date))
      (try
        (success-message (raw-experiments-create<! experiment))
        (catch Exception e (error-message "Errors in referencces!")))
      (error-message "Errors in 'start date or 'stop date'!"))))
