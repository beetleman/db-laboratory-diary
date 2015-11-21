(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery]]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "sqlite.db"
   })

(defquery tables "db/tables.sql" {:connection db})
