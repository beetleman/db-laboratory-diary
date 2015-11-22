(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery]]
            [environ.core :refer [env]]))

(def db
  {:classname (get-in env [:db :classname])
   :subprotocol (get-in env [:db :subprotocol])
   :user (get-in env [:db :user])
   :password (get-in env [:db :password])
   :subname (get-in env [:db :subname])
   })

(defquery tables "db/tables.sql" {:connection db})
