(ns db-laboratory-diary.server
  (:require [db-laboratory-diary.handler :refer [app]]
            [db-laboratory-diary.db :refer [default-admin-create<!]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (default-admin-create<!)
    (run-jetty app {:port port :join? false})))
