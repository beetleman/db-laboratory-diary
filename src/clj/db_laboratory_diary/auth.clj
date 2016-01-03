(ns db-laboratory-diary.auth
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [db-laboratory-diary.db :as db]))



;; PASSWORD

(defn password-is-eq?
  "compare password with hash"
  [password hashed]
  (creds/bcrypt-verify password hashed))

(defn current-user
  "get current user from request"
  [req]
  (if-let [identity (friend/identity req)]
    (dissoc (get-in identity [:authentications (:current identity)])
            :roles)
    nil))

(defn load-roles
  "load roles into user"
  [user]
  (if (:is_admin user)
    (assoc user :roles  #{::admin})
    (assoc user :roles  #{::user})))

(derive ::admin ::user)

(defn credential-fn
  "credential function for friend"
  [{:keys [username password]}]
  (when-let [user (first (db/raw-users-get-by-username
                          {:username username}))]
    (when (password-is-eq? password (:password user))
      (load-roles (dissoc user :password)))))
