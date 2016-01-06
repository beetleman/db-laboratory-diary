(ns db-laboratory-diary.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [goog.dom.forms :as gforms]
            [ajax.core :refer [GET POST]]))

;; -------------------------
;; Auth
(defn is_user? [{:keys [about]}]
  (:current-user about))

(defn is_admin? [{:keys [about]}]
  (if-let [user (is_user? {:about about})]
    (if (:is_admin user)
      user
      nil)))

(defn is_anybody? [{:keys [about]}]
  {})

;; -------------------------
;; API

(def api
  "/api")

(def app-state (atom {}))
(add-watch app-state :logger #(-> %4 clj->js js/console.debug))

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


(defn api-get [state state-target url {:keys [] :as extra}]
  (let [extra (merge {:headers (authorization-header @state)
                      :error-handler error-handler
                      :handler-fn assoc}
                     extra)]
    (GET
     (api-url url)
     {:keywords? true
      :response-format :json
      :headers (:headers extra)
      :error-handler (:error-handler extra)
      :handler (fn [response]
                 (swap! state
                        (:handler-fn extra)
                        state-target
                        response))})))

(defn api-post [state state-target url params {:keys [] :as extra}]
  (let [extra (merge {:headers (authorization-header @state)
                      :error-handler error-handler
                      :handler-fn assoc}
                     extra)]
    (POST
     (api-url url)
     {:params params
      :keywords? true
      :format :raw
      :response-format :json
      :headers (:headers extra)
      :error-handler (:error-handler extra)
      :handler (fn [response]
                 (swap! state
                        (:handler-fn extra)
                        state-target
                        response))})))

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
  [{:name "About" :href "/about" :auth is_user?}
   {:name "Home" :href "/" :auth is_anybody?}])

(defn header-links [state]
  (into [:ul {:class "nav navbar-nav"}]
        (map (fn [a]
               (let [class (if (= (a :href) (:current-path @state))
                             "active"
                             "")]
                 [:li {:class class}
                  [:a {:href (:href a)} (:name a)]]))
             (filter (fn [v] ((:auth v) @state))  header-links-def))))

(defn login-btn [state]
  [:a {:href "login"}
   [:button {:type "submit" :class "btn btn-success"}
    "Sign in"]])


(defn header-login [state]
  [:div {:class "nav navbar-form navbar-right"}
   (if-let [user (is_user? @state)]
     [:a {:href "/logout"}
      [:button {:type "submit" :class "btn btn-success"}
       "Logout " [:span (:username user)]]]
     [login-btn state])])


(defn header [state]
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
     [header-links state]
     [header-login state]]]])


(defn home-page [state]
  [:div {:class "container"}
   [:h2 "Welcome to db-laboratory-diary"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page [state]
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


(defn submit-login [state event]
  (.preventDefault event)
  (let [user (form->map (.-target event))]
    (api-post
     state :user "check-credencials" user
     {:headers {}
      :handler-fn (fn [a k v]
                    (if (:username v)
                      (do
                        (accountant/navigate! "/")
                        (assoc a k user))
                      (-> a (assoc k nil)
                          (assoc :danger-msg
                                 "Bad login or password"))))})))

(defn login-page [state]
  [:div {:class "container"}
   [:form {:class "form-signin" :on-submit #(submit-login state %)}
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

;; -------------------------
;; Messages
(defn msg* [state state-target type]
  (when-let [msg (state-target @state)]
    [:div {:class (str "alert alert-" type) :role "alert"
           :on-click #(swap! state dissoc state-target)} msg]))

(defn danger-msg [state]
  (msg* state :danger-msg "danger"))

(defn success-msg [state]
  (msg* state :success-msg "danger"))

(defn current-page [state]
  [:div
   [header state]
   [danger-msg state]
   [success-msg state]
   (if ((session/get :is-auth?) @state)
     [(session/get :current-page) state]
     [:div {:class "container"}
      [:h2 "Error"]
      [:p "You do not have sufficient permissions to access this page"]
      [login-btn state]])])

;; -------------------------
;; Routes
(defn set-current-path [state]
  (swap! state assoc :current-path
         (aget js/window
               "location"
               "pathname")))

(defn site [state page is-auth? & api-todo]
  (let [todo (into [[:about "about"]] api-todo)]
    (doall (map (fn [a] (apply api-get (into [state] a)))
                todo))
    (set-current-path state)
    (swap! state dissoc state :danger-msg)
    (swap! state dissoc state :success-msg)
    (session/put! :is-auth? is-auth?)
    (session/put! :current-page page)))

(secretary/defroute "/" []
  (site app-state #'home-page is_anybody?))

(secretary/defroute "/about" []
  (site app-state #'about-page is_user?))

(secretary/defroute "/login" []
  (site app-state #'login-page is_anybody?))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page app-state] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
