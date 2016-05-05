(ns db-laboratory-diary.db
  (:require [yesql.core :refer [defquery defqueries]]
            [clojure.java.jdbc :as jdbc]
            [cheshire.generate :refer [add-encoder encode-str remove-encoder]]
            [cheshire.custom :as custom]
            [environ.core :refer [env]]
            [clj-time.jdbc]
            [clj-time.core :as timec]
            [clj-time.coerce :as timecoerce]
            [clj-time.format :as timef]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(def db (env :db-url))

(defquery tables "db/tables.sql" {:connection db})


;; TYPE EXTENSIONS
(add-encoder  org.joda.time.DateTime
              (fn [c jg]
                (custom/encode-long (timecoerce/to-long c) jg)))


;; UTILS
(defmulti str->int class)
(defmethod str->int String [s]
  (try
    (Integer. s)
    (catch Exception e nil)))
(defmethod str->int Number [n]
  n)

(defn str->vector-int [s]
  (if (nil? s)
    []
    (filterv (complement nil?)
             (map str->int (clojure.string/split s #",")))))

(defn str->boolean [s]
  (cond
    (= s "false") false
    (= s "true") true
    :default (boolean s)))

(def error-message
  "deffault error mesage"
  "Wrong data!")

(defn get-error-message
  ([message] (get-error-message message nil))
  ([message e]
   (let [e (if ((complement nil?) e)
             (.getMessage e))]
     {:data nil :error message :raw-error e})))

(defn get-success-message [data]
  {:data data :error nil})


(defn convert [data converters]
  (reduce-kv (fn [m k v]
               (assoc m k (v (k data))))
             data converters))

(defn query-with-message [query error-message converters args]
  (let [args (convert args converters)]
    (try
      (get-success-message (query args))
      (catch Exception e (get-error-message error-message e)))))

(defmacro defquery-with-message
  ([query-name query]
   `(defquery-with-message ~query-name ~query error-message {}))
  ([query-name query error-message]
   `(defquery-with-message ~query-name ~query ~error-message {}))
  ([query-name query error-message converters]
   `(defn ~query-name [args#]
      (query-with-message ~query ~error-message ~converters args#))))

;; USERS

(def user-fields-public [:id :username :lastname :firstname])
(def user-fields-priv [:id :username :lastname :firstname :is_admin :email])

(defqueries "db/users.sql" {:connection db})

(defn hash-password
  "hash passwprd using bcrypt"
  [to-hash]
  (creds/hash-bcrypt to-hash))

(def default-admin-user
  {:name "admin"
   :firstname nil
   :lastname nil
   :username "admin"
   :email "admin@fake.pl"
   :password "qwerty1"
   :is_admin true})

(defn user-count []
  (-> (raw-users-count) first :count))

(defn users-create<!
  "create new user with hashing password"
  [user]
  (let [user (merge
              {:firstname nil
               :lastname nil
               :password "password"}
              user)]
    (try
      (get-success-message
       (select-keys
        (raw-users-create<! (update user :password hash-password))
        user-fields-priv))
      (catch Exception e
        (get-error-message "Username or password exists in db!" e)))))

(defn user-all
  ([]
   (user-all false))
  ([admin?]
   (let [fields (if admin?
                  user-fields-priv
                  user-fields-public)]
     (map (fn [user] (select-keys user fields))
          (raw-users-all)))))

(defn users-save!
  "update user with hashing password"
  [id user]
  (let [old-user (raw-users-get {:id id})
        new-user (merge old-user user)
        password (if (:password user)
                   (hash-password (:password user))
                   (:password old-user))]
    (raw-users-save! (assoc new-user :password password))))

(defn default-admin-create<!
  "if users table is empty create default admin with:
  `username': `admin'
  `passord': `qwerty1'"
  []
  (if (zero? (user-count))
    (users-create<! default-admin-user)))

;; AREA_DATA

(defqueries "db/area_data.sql" {:connection db})
(defquery-with-message area_data-create<! raw-area_data-create<! error-message
  {:max_area str->int})


;; SURFACES

(defqueries "db/surfaces.sql" {:connection db})

(defn add-mesurments-to-expetiment<! [mesurments]
  (try (jdbc/with-db-transaction [tx db]
         (mapv #(raw-add-mesurment-to-surfaces<! (convert % {:surface_id str->int
                                                             :success str->boolean})
                                                 {:connection tx}) mesurments)
         (get-success-message "OK"))
       (catch Exception e (get-error-message "Something bad happened!" e))))


;; EXPERIMENTS

(defqueries "db/experiments.sql" {:connection db})

(defn update-experiment-laborants!
  ([experiment laborants_ids]
   (update-experiment-laborants! experiment laborants_ids db))
  ([experiment laborants_ids tx]
   (let [experiment_id (:id experiment)
         old (map :id (raw-laborants_experiments-for-experiment
                       {:experiment_id experiment_id}))
         old (set old)
         new (set laborants_ids)
         to-add (clojure.set/difference new old)
         to-delete (clojure.set/difference old new)]
     (mapv (fn [id] (raw-add-laborant-to-experiment<!
                     {:experiment_id experiment_id
                      :laborant_id id}
                     {:connection tx}))
           to-add)
     (mapv (fn [id] (raw-delete-laborant-from-experiment!
                     {:experiment_id experiment_id
                      :laborant_id id}
                     {:connection tx}))
           to-delete)
     (get-success-message (assoc experiment :laborants_ids laborants_ids)))))

(defn add-surfaces-to-experiment<!* [experiment tx]
  (let [area_data (-> (raw-area_data-get {:id (:area_data experiment)}) first)]
    (mapv (fn [_]
            (raw-add-surface-to-experiment<! {:experiment_id (:id experiment)
                                              :area_id (:id area_data)}
                                             {:connection tx}))
          (-> area_data :max_area range))))

(defn experiments-create<! [experiment]
  (let [laborants_ids (-> experiment :laborants_ids str->vector-int)
        experiment (convert (dissoc experiment :laborants_ids)
                            {:manager_id str->int
                             :area_data_id str->int
                             :fertilizer boolean
                             :stop_date timef/parse
                             :start_date timef/parse})
        start_date (:start_date experiment)
        stop_date (:stop_date experiment)]
    (if (and stop_date start_date (timec/after? stop_date start_date))
      (try (jdbc/with-db-transaction [tx db]
             (let [experiment (raw-experiments-create<! experiment {:connection tx})]
               (add-surfaces-to-experiment<!* experiment tx)
               (update-experiment-laborants! experiment laborants_ids tx)))
           (catch Exception e (get-error-message "Errors in references!" e)))
      (get-error-message "Errors in 'start date' or 'stop date'!"))))


(defn experiments-get [{:keys [id]}]
  (let [id (str->int id)]
    (if-let [experiment (first (raw-experiments-get {:id id}))]
      (merge experiment
             {:manager (-> (raw-users-get
                            {:id (:manager experiment)})
                           first
                           (select-keys user-fields-public))
              :area_data (first (raw-area_data-get
                                 {:id (:area_data experiment)}))
              :surfaces (raw-surfaces-get-by-experiment
                         {:experiment_id id})
              :laborants (map #(select-keys % user-fields-priv)
                              (raw-all-laborant-for-experiment
                               {:experiment_id id}))}))))

(defn all-experiments-for-user-all [args]
  (let [experiments (raw-all-experiments-for-user-all args)
        laborants_experiments (group-by :experiment (raw-all-laborants_experiments))]
    (map (fn [e] (assoc e :laborants_ids
                        (map :laborant (-> e :id laborants_experiments))))
         experiments)))

(defquery-with-message
  add-surface-to-experiment<!
  raw-add-surface-to-experiment<!
  "Bad surface area or experiment dont exist")

(defquery-with-message
  add-laborant-to-experiment<!
  raw-add-laborant-to-experiment<!
  "Bad user id or experiment dont exist")

(defquery-with-message
  delete-laborant-from-experiment!
  raw-delete-laborant-from-experiment!
  "Laborant or experiment dont exist")

(defquery-with-message
  add-mesurment-to-surfaces<!
  raw-add-mesurment-to-surfaces<!
  "Surface dont exist or or duble mesurment")

(defquery-with-message
  all-mesurments-for-surfaces
  raw-all-mesurments-for-surfaces
  "Surface dont exist")


(defquery-with-message
  all-mesurments-for-experiment
  raw-all-mesurments-for-experiment
  "Experiment dont exist"
  {:experiment_id str->int})
