(ns db-laboratory-diary.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [db-laboratory-diary.auth :as auth]
            [db-laboratory-diary.api :as api]
            [db-laboratory-diary.base :refer [current-page]]
            [db-laboratory-diary.about :refer [about-page]]
            [db-laboratory-diary.login :refer [login-page]]
            [db-laboratory-diary.users :refer [users-page]]
            [db-laboratory-diary.experiments :refer [experiments-page
                                                     experiment-page]]
            [db-laboratory-diary.my-experiments :refer [my-experiments-page
                                                        my-experiment-page
                                                        my-experiment-add-mesurment-page]]
            [db-laboratory-diary.area_data :refer [area_data-page]]
            [db-laboratory-diary.home :refer [home-page]]))

;; ------------------------
;; app state
(def app-state (atom {}))


;; ------------------------
;; helpers
(defn set-current-path [state]
  (swap! state assoc :current-path
         (aget js/window
               "location"
               "pathname")))

(defn site [state page is-auth? & api-todo]
  (let [todo (into [[:about "about"]] api-todo)]
    (doall (map (fn [a] (apply api/api-get (into [state] a)))
                todo))
    (set-current-path state)
    (swap! state dissoc :danger-msg)
    (swap! state dissoc :success-msg)
    (swap! state assoc :page-state :view)
    (session/put! :is-auth? is-auth?)
    (session/put! :current-page page)))


;; -------------------------
;; Initialize app

(secretary/defroute "/" []
  (site app-state #'home-page auth/is_anybody?))

(secretary/defroute "/about" []
  (site app-state #'about-page auth/is_user?))

(secretary/defroute "/login" []
  (site app-state #'login-page auth/is_anybody?))

(secretary/defroute "/users" []
  (site app-state #'users-page auth/is_admin?
        [:users "users"]))

(secretary/defroute "/my-experiments" []
  (site app-state #'my-experiments-page auth/is_user?
        [:my-experiments "my_experiments"]))


(secretary/defroute "/my-experiments/:id" {:as params}
  (site app-state #'my-experiment-page auth/is_user?
        [:my-experiment-mesurments (str "experiments" "/" (:id params)  "/mesurments")]
        [:my-experiment (str "experiments" "/" (:id params))]))


(secretary/defroute "/my-experiments/:id/add-mesurment" {:as params}
  (site app-state #'my-experiment-add-mesurment-page auth/is_user?
        [:my-experiments "my_experiments"]
        [:my-experiment (str "experiments" "/" (:id params))]))

(secretary/defroute "/experiments" []
  (site app-state #'experiments-page auth/is_admin?
        [:experiments "experiments"]
        [:area_data "area_data"]
        [:users "users"]))

(secretary/defroute "/experiments/:id" {:as params}
  (site app-state #'experiment-page auth/is_admin?
        [:experiment (str "experiments" "/" (:id params))]
        [:area_data "area_data"]
        [:users "users"]))


(secretary/defroute "/area_data" []
  (site app-state #'area_data-page auth/is_admin?
        [:area_data "area_data"]))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page app-state] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
