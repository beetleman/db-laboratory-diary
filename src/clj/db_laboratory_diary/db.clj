(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery]]
            [environ.core :refer [env]]))

(def db
  {:classname (get-in env [:db :classname])
   :subprotocol (get-in env [:db :subprotocol])
   :subname (get-in env [:db :subname])
   })

(defquery tables "db/tables.sql" {:connection db})
