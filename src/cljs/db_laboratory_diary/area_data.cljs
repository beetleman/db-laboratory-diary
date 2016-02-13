(ns db-laboratory-diary.area_data
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [db-laboratory-diary.api :as api]
            [reagent-forms.core :refer [bind-fields]]
            [accountant.core :as accountant]
            [reagent.core :as r]))



(defn area_data-table [state]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"}
    "Area data list"
    [:div.pull-right
     [:button {:class "btn btn-success btn-xs" :on-click (e/on-add state)}
      "Add"]]]
   [tables/table (:area_data @state)
    {:name "Name"
     :address "Address"
     :max_area "Max"}]])


(defn on-add-area_data-fn [state]
  (form/on-add-fn state :area_data "area_data" "/area_data" "Area data added"
                  (fn [area_data] area_data)))


(defn add-area_data-form [state]
  [:form {:on-submit (on-add-area_data-fn state)}
   [form/form-group-input "Name" "name" {:maxlength 100}]
   [form/form-group-input "Address" "address" {:maxlength 200}]
   [form/form-group-input "Max" "max_area"  {:type "number"}]
   [form/form-yes-no (e/on-view state)]])


(defn area_data-page [state]
  [:div {:class "container"}
   [:h2 "Area data"]
   (if (= :add (:page-state @state))
     [add-area_data-form state]
     [area_data-table state])])
