(ns db-laboratory-diary.users
  (:require [db-laboratory-diary.tables :as tables]))

(defn users-table [state]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"} "Users list"]
   [tables/table (:users @state)
    {:username "Username"
     :lastname "Lastname"
     :firstname "Firstname"
     :email "Email"
     :is_admin "Admin"}]])

(defn users-page [state]
  [:div {:class "container"}
   [:h2 "Users"]
   [users-table state]])
