(ns db-laboratory-diary.users
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [db-laboratory-diary.api :as api]
            [reagent-forms.core :refer [bind-fields]]
            [accountant.core :as accountant]
            [reagent.core :as r]))

(defn users-table [state]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"} "Users list"
    [:div.pull-right
     [:button {:class "btn btn-success btn-xs" :on-click (e/on-add state)}
      "Add"]]]
   [tables/table (:users @state)
    {:username "Username"
     :lastname "Lastname"
     :firstname "Firstname"
     :email "Email"
     :is_admin "Admin"}]])

(defn on-add-user [state event]
  (.preventDefault event)
  (let [user (form/form->map (.-target event))
        user (if (:is_admin user) (assoc user :is_admin true)
                 (assoc user :is_admin false))]
    (api/api-post
     state :user "users" user
     {:handler-fn (fn [a k v]
                    (if-let [error (:error v)]
                      (assoc a :danger-msg error)
                      (do
                        (accountant/navigate! "/users")
                        (assoc a :info-msg
                               "User added"))))})))

(defn add-user-form [state]
  [:form {:on-submit (partial on-add-user state)}
   [form/form-group-input "Username" "username" {:pattern ".{5,}"}]
   [form/form-group-input "Email" "email" {:type "email" :pattern ".{5,}"}]
   [form/form-checkbox "Admin" "is_admin"]
   [form/form-yes-no (e/on-view state)]])


(defn users-page [state]
  [:div {:class "container"}
   [:h2 "Users"]
   (if (= :add (:page-state @state))
     [add-user-form state]
     [users-table state])])
