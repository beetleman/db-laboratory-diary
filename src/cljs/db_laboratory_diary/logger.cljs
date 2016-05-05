(ns db-laboratory-diary.db.logger)

(defn debug [key x]
  (-> {key x} clj->js js/console.debug))
