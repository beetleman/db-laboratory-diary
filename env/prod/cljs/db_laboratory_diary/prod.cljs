(ns db-laboratory-diary.prod
  (:require [db-laboratory-diary.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
