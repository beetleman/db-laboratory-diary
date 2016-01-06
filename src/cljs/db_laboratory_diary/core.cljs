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
            [db-laboratory-diary.home :refer [home-page]]))

;; ------------------------
;; app state
(def app-state (atom {}))
(add-watch app-state :logger #(-> %4 clj->js js/console.debug))


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
    (swap! state dissoc state :danger-msg)
    (swap! state dissoc state :success-msg)
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


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page app-state] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
