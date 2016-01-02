(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery defqueries]]
            [environ.core :refer [env]]))

(def db (env :db-url))

(defquery tables "db/tables.sql" {:connection db})


;; PASSWORD

(defn hash-password
  "hash passwprd using sha1"
  [to-hash]
  (apply str
         (map (partial format "%02x")
              (.digest (doto (java.security.MessageDigest/getInstance "sha1")
                         .reset
                         (.update (.getBytes to-hash)))))))

(defn password-is-eq?
  "compare password with hash"
  [password hashed]
  (= (hash-password password) hashed))


;; USERS
(defqueries "db/users.sql" {:connection db})

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
  (raw-users-create<! (update user :password hash-password)))

(defn user-all
  ([]
   (user-all false))
  ([admin?]
   (let [fields (if admin?
                  [:id :username :lastname :firstname :is_admin :email]
                  [:id :username :lastname :firstname])]
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
