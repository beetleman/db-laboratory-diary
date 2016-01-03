(ns db-laboratory-diary.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [goog.dom.forms :as gforms]
            [ajax.core :refer [GET POST]]))

;; -------------------------
;; Auth
(defn is_auth? [{:keys [about]}]
  (:current-user about))

;; -------------------------
;; API

(def api
  "/api")

(def state (atom {}))
(add-watch state :logger #(-> %4 clj->js js/console.debug))

(defn api-url [url]
  (str api "/" url))

(defn authorization-header [{:keys [user]}]
  (if user
    {:authorization (str "Basic " (js/btoa
                                   (str
                                    (:username user)
                                    ":"
                                    (:password user))))}
    {}))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn api-fetch [url state-target {:keys [] :as extra}]
  (let [extra (merge {:headers (authorization-header @state)
                      :handler-fn assoc}
                     extra)]
    (GET
     (api-url url)
     :keywords? true
     :response-format :json
     :headers (:headers extra)
     :error-handler error-handler
     :handler (fn [response]
                (swap! state
                       (:handler-fn extra)
                       state-target
                       response)))))

;; -------------------------
;; Views

(defn form->map [form]
  (let [form-map (gforms/getFormDataMap form)]
    (reduce
     (fn [result key]
       (let [value (.get form-map key)]
         (if (and value (= 1 (count value)))
           (assoc result (keyword key) (first value))
           (assoc result (keyword key) value))))
     {}
     (.getKeys form-map))))

(def header-links-def
  [{:name "About" :href "/about"}
   {:name "Home" :href "/"}])

(defn header-links [{:keys [current-path]}]
  (into [:ul {:class "nav navbar-nav"}]
        (map (fn [a]
               (let [class (if (= (a :href) (:current-path @state))
                             "active"
                             "")]
                 [:li {:class class}
                  [:a {:href (:href a)} (:name a)]]))
             header-links-def)))

(defn login-btn []
  [:div {:class "nav navbar-form navbar-right"}
   (if-let [user (is_auth? @state)]
     [:a {:href "/logout"}
      [:button {:type "submit" :class "btn btn-success"}
       "Logout " [:span (:username user)]]]
     [:a {:href "login"}
      [:button {:type "submit" :class "btn btn-success"}
       "Sign in"]]
     )])

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
     [header-links]
     [login-btn]]]])


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


(defn submit-login [event]
  (.preventDefault event)
  (let [user (form->map (.-target event))]
    (api-fetch "is-auth"
               :user
               {:headers (authorization-header {:user user})
                :handler-fn (fn [a k v]
                              (if (:username v)
                                (do
                                  (accountant/navigate! "/")
                                  (assoc a k user))
                                (do
                                  (accountant/navigate! "/login")
                                  (assoc a k nil))))})))

(defn login-page []
  [:div {:class "container"}
   [:form {:class "form-signin" :on-submit submit-login}
    [:h2 {:class "form-signin-heading"} "Please sign in"]
    [:label {:for "inputUsername" :class"sr-only"} "Username"]
    [:input {:id "inputUsername" :name "username"
             :pattern ".{5,}" :required true :title "5 characters minimum"
             :class "form-control" :placeholder "Username"}]
    [:label {:for "inputPassword" :class "sr-only"} "Password"]
    [:input {:type "password" :id "inputPassword" :name "password"
             :pattern ".{5,}" :required true :title "5 characters minimum"
             :class "form-control" :placeholder "Password"}]
    [:button {:class "btn btn-lg btn-primary btn-block" :type "submit"}
     "Sign in"]]])


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

(secretary/defroute "/login" []
  (site #'login-page))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
