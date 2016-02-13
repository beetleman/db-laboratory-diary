(ns db-laboratory-diary.users
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [reagent-forms.core :refer [bind-fields]]
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


(defn on-add-user-fn [state]
  (form/on-add-fn state :user "users" "/users" "User added"
                  (fn [user] (if (:is_admin user)
                               (assoc user :is_admin true)
                               (assoc user :is_admin false)))))


(defn add-user-form [state]
  [:form {:on-submit (on-add-user-fn state)}
   [form/form-group-input "Username" "username"]
   [form/form-group-input "Email" "email" {:type "email"}]
   [form/form-checkbox "Admin" "is_admin"]
   [form/form-yes-no (e/on-view state)]])


(defn users-page [state]
  [:div {:class "container"}
   [:h2 "Users"]
   (if (= :add (:page-state @state))
     [add-user-form state]
     [users-table state])])
