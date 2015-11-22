(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery defqueries]]
            [environ.core :refer [env]]))

(def db
  {:classname (get-in env [:db :classname])
   :subprotocol (get-in env [:db :subprotocol])
   :user (get-in env [:db :user])
   :password (get-in env [:db :password])
   :subname (get-in env [:db :subname])
   })

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
