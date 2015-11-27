(ns db-laboratory-diary.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [ajax.core :refer [GET POST]]))

;; -------------------------
;; Views

(def api
  "/api")

(def state (atom {}))

(defn api-url [url]
  (str api "/" url))


(defn home-page []
  [:div [:h2 "Welcome to db-laboratory-diary"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div
   [:h2 "About" (get-in @state [:about :name])]
   [:span "Version: " (get-in @state [:about :version])]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (do
    (GET
     (api-url "about")
     :keywords? true
     :response-format :json
     :handler (fn [response]
                (swap! state assoc :about response)))
    (session/put! :current-page #'about-page)))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
