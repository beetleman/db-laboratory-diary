(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery defqueries]]
            [environ.core :refer [env]]))

(defquery tables "db/tables.sql" {:connection (env :db-url)})


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

(defn users-create<!
  "create new user with hashing password"
  [user]
  (raw-users-create<! (update user :password hash-password)))

(defn users-save!
  "update user with hashing password"
  [id user]
  (let [old-user (raw-users-get {:id id})
        new-user (merge old-user user)
        password (if (:password user)
                   (hash-password (:password user))
                   (:password old-user))]
    (raw-users-save! (assoc new-user :password password))))
