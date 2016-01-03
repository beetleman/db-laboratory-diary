(ns db-laboratory-diary.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [ajax.core :refer [GET POST]]))


;; -------------------------
;; API

(def api
  "/api")

(def state (atom {}))
(add-watch state :logger #(-> %4 clj->js js/console.debug))

(defn api-url [url]
  (str api "/" url))

(defn api-fetch [url state-target]
  (GET
   (api-url url)
   :keywords? true
   :response-format :json
   :handler (fn [response]
              (swap! state assoc state-target response))))

;; -------------------------
;; Views

(def header_links
  [{:name "About" :href "/about"}
   {:name "Home" :href "/"}])

(defn header []
  [:nav {:class "navbar navbar-inverse navbar-fixed-top"}
   [:div {:class "container"}
    [:div {:class "navbar-header"}
     [:button {:type "button"
               :class "navbar-toggle collapsed"
               :data-toggle "collapse"
               :data-target "#navbar"
               :aria-expanded "false"
               :aria-controls "navbar"}
      [:span {:class "sr-only"}
       "Toggle navigation"]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]]
     [:a {:class "navbar-brand" :href "/"} (get-in @state [:about :name])]]
    [:div {:class "collapse navbar-collapse" :id "navbar"}
     (into [:ul {:class "nav navbar-nav"}]
           (map (fn [a]
                  (let [class (if (= (a :href) (@state :current-path))
                                "active"
                                "")]
                    [:li {:class class}
                     [:a {:href (:href a)} (:name a)]]))
                header_links))]]])


(defn home-page []
  [:div {:class "container"}
   [:h2 "Welcome to db-laboratory-diary"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div {:class "container"}
   [:h2 "About"]
   [:ul
    [:li "Name: " (get-in @state [:about :name])]
    [:li "Version: " (get-in @state [:about :version])]
    [:li "Tables:"
     (into [:ul ]
           (map (fn [d] [:li (:table_name d)])
                (get-in @state [:about :tables])))]]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [header] [(session/get :current-page)]])


;; -------------------------
;; Routes
(defn set-current-path []
  (swap! state assoc :current-path
         (aget js/window
               "location"
               "pathname")))

(defn site [page & api-todo]
  (let [todo (into [["about" :about]] api-todo)]
    (doall (map (fn [a] (apply api-fetch a))
                todo))
    (set-current-path)
    (session/put! :current-page page)))

(secretary/defroute "/" []
  (site #'home-page))

(secretary/defroute "/about" []
  (site #'about-page))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
