(ns db-laboratory-diary.my-experiments
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [accountant.core :as accountant]
            [db-laboratory-diary.date :as date]
            [db-laboratory-diary.experiments :as experiments]
            [db-laboratory-diary.db.logger :as logger]
            [reagent-forms.core :refer [bind-fields]]
            [reagent.core :as r]))



(defn experiments-table [state]
  (let [user_id (get-in @state [:about :current-user :id])]
    (logger/debug :user_id user_id)
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
      (fn [data] (if (some #(= % user_id) (:laborants_ids data))
                   [:a {:href (str "/my-experiments/" (:id data) "/add-mesurment")}
                    "add mesurment"]))]]))


(defn my-experiments-page [state]
  [:div {:class "container"}
   [:h2 "My experiments"]
   [experiments-table state]])


(defn experiment-info [experiment]
  [:dl.dl-horizontal
   [:dt "Area address"] [:dd (get-in experiment [:area_data :address])]
   [:dt "Area name"] [:dd (get-in experiment [:area_data :name])]
   [:dt "Fertilizer"] [:dd (-> experiment :fertilizer str)]])


(defn my-experiment-page [state]
  [:div {:class "container"}
   [:h2 (str "Experiment #" (get-in @state [:my-experiment :id]))]
   [experiments/experiment-details (:my-experiment @state)]])


(defn my-experiment-add-mesurment-page [state]
  [:div {:class "container"}
   [:h2 (str "Add mesurment to experiment #" (get-in @state [:my-experiment :id]))]
   [experiment-info (:my-experiment @state)]])
