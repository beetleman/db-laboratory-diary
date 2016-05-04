(ns db-laboratory-diary.my-experiments
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [accountant.core :as accountant]
            [db-laboratory-diary.date :as date]
            [reagent-forms.core :refer [bind-fields]]
            [reagent.core :as r]))



(defn experiments-table [state]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"}
    "My experiments list"]
   [tables/table (map (fn [e]
                        (merge e {:start_date (-> e :start_date date/parse)
                                  :stop_date (-> e :stop_date date/parse)}))
                      (:my-experiments @state))
    {:start_date "Start"
     :stop_date "Stop"
     :name "Area name"
     :address "Area address"
     :fertilizer "Fertilizer"}
    (fn [data] [:a {:href (str "/my-experiments/" (:id data))}
                "detail"])
    (fn [data] (if (not= (:manager data) (get-in @state [:about :current-user :id]))
                   [:a {:href (str "/my-experiments/" (:id data) "/add-mesurment")}
                    "add mesurment"]))]])


(defn my-experiments-page [state]
  [:div {:class "container"}
   [:h2 "My experiments"]
   [experiments-table state]])


(defn my-experiment-page [state]
  [:div {:class "container"}
   [:h2 "My experiments"]
   (if (= :add (:page-state @state))
     [:span "yolo"]
     [:span "yolo"])])


(defn my-experiment-add-mesurment-page [state]
  [:div {:class "container"}
   [:h2 "Add mesurment"]
   (if (= :add (:page-state @state))
     [:span "yolo"]
     [:span "yolo"])])
