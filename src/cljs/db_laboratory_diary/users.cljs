(ns db-laboratory-diary.users
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
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

(defn add-user-form [state]
  [:h3 "Add"]
  [:button {:class "btn btn-success btn-xs" :on-click (e/on-view state)}
   "Cancel"])

(defn users-page [state]
  [:div {:class "container"}
   [:h2 "Users"]
   (if (= :add (:page-state @state))
     [add-user-form state]
     [users-table state])])
