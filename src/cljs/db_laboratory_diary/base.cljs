(ns db-laboratory-diary.base
  (:require [reagent.session :as session]
            [db-laboratory-diary.auth :as auth]))

(defn msg* [state state-target type]
  (when-let [msg (state-target @state)]
    [:div {:class (str "alert alert-" type) :role "alert"
           :on-click #(swap! state dissoc state-target)} msg]))

(defn danger-msg [state]
  (msg* state :danger-msg "danger"))

(defn success-msg [state]
  (msg* state :success-msg "danger"))

(defn login-btn [state]
  [:a {:href "/login"}
   [:button {:type "submit" :class "btn btn-success"}
    "Sign in"]])

(defn header-login [state]
  [:div {:class "nav navbar-form navbar-right"}
   (if-let [user (auth/is_user? @state)]
     [:a {:href "/logout"}
      [:button {:type "submit" :class "btn btn-success"}
       "Logout " [:span (:username user)]]]
     [login-btn state])])

(def header-links-def
  [{:name "About" :href "/about" :auth auth/is_user?}
   {:name "Home" :href "/" :auth auth/is_anybody?}
   {:name "Users" :href "/users" :auth auth/is_admin?}
   {:name "Experiments" :href "/experiments" :auth auth/is_user?}
   {:name "Area data" :href "/area_data" :auth auth/is_user?}])

(defn header-links [state]
  (into [:ul {:class "nav navbar-nav"}]
        (map (fn [a]
               (let [class (if (= (a :href) (:current-path @state))
                             "active"
                             "")]
                 [:li {:class class}
                  [:a {:href (:href a)} (:name a)]]))
             (filter (fn [v] ((:auth v) @state))  header-links-def))))

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

(defn loading-spinner [state]
  (when (:loading @state)
    [:div.cover
     [:div.spinner-container
      [:i {:class "fa fa-cog fa-5x fa-spinner fa-pulse"}]]]))

(defn current-page [state]
  [:div
   [header state]
   [loading-spinner state]
   [danger-msg state]
   [success-msg state]
   (if ((session/get :is-auth?) @state)
     [(session/get :current-page) state]
     [:div {:class "container"}
      [:h2 "Error"]
      [:p "You do not have sufficient permissions to access this page"]
      [login-btn state]])])
