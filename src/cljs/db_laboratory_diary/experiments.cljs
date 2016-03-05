(ns db-laboratory-diary.experiments
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [reagent-forms.core :refer [bind-fields]]
            [reagent.core :as r]))

(defn experiments-table [state]
  [tables/header state "Experiments list"
   [tables/table (:experiments @state)
    {:start_date "Start"
     :stop_date "Stop"
     :fertilizer "Fertilizer"}]])


(defn on-add-experiment-fn [state]
  (form/on-add-fn state :experiment "experiments" "/experiments" "Experiment added"
                  (fn [experiment] experiment)))


(defn add-experiment-form [state]
  [:form {:on-submit (on-add-experiment-fn state)}
   [form/form-group-select (:users @state) {:value :id :text :username} "Manager" "manager_id"]
   [form/form-group-select (:users @state) {:value :id :text :username} "Laborants" "laborants_ids"
    {:multiple true}]
   [form/form-group-select (:area_data @state) {:value :id :text :address} "Area data" "area_data_id"]
   [form/form-group-input "Start" "start_date" {:type "date"}]
   [form/form-group-input "End" "stop_date" {:type "date"}]
   [form/form-checkbox "Fertilizer" "fertilizer"]
   [form/form-yes-no (e/on-view state)]])


(defn experiments-page [state]
  [:div {:class "container"}
   [:h2 "Experiments"]
   (if (= :add (:page-state @state))
     [add-experiment-form state]
     [experiments-table state])])
