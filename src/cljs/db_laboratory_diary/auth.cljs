(ns db-laboratory-diary.auth)

(defn is_user? [{:keys [about]}]
  (:current-user about))

(defn is_admin? [{:keys [about]}]
  (if-let [user (is_user? {:about about})]
    (if (:is_admin user)
      user
      nil)))

(defn is_anybody? [{:keys [about]}]
  {})
