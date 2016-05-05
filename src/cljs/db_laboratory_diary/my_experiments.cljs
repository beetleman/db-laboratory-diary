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



(defn add-mesurment-surface-radio [id text callback checked]
  [:div.radio
   [:label
    [:input {:type "radio"
             :on-change callback
             :checked checked
             :name (str "surface-" id)}
     text]]])


(defn add-mesurment-surface-header [state surfaces table]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"}
    "Mesurments"
    [:div.pull-right
     [:button {:class "btn btn-success btn-xs"
               :on-click (fn [e] (logger/debug :save e))
               :disabled (not= (count surfaces) (count (keys @state)))}
      "Save"]]]
   table])


(defn add-mesurment-surface [state surface]
  (let [id (:id surface)]
    [:td
     [add-mesurment-surface-radio
      id "Yes" (fn [e] (swap! state assoc id true))
      (= (get @state id) true)]
     [add-mesurment-surface-radio
      id "No" (fn [e] (swap! state assoc id false))
      (= (get @state id) false)]]))

(defn add-mesurment [state colls]
  (let [data (r/atom {})
        surfaces (get-in @state [:my-experiment :surfaces])
        rows (partition-all colls surfaces)]
    [add-mesurment-surface-header
     data
     surfaces
     [:table.table.table-bordered
      [:tbody
       (map-indexed
        (fn [idx r]
          ^{:key idx} [:tr
                       (map
                        (fn [s]
                          ^{:key (:id s)} [add-mesurment-surface data s])
                        r)])
        rows)]]]))


(defn my-experiment-page [state]
  [:div {:class "container"}
   [:h2 (str "Experiment #" (get-in @state [:my-experiment :id]))]
   [experiments/experiment-details (:my-experiment @state)]])


(defn my-experiment-add-mesurment-page [state]
  [:div {:class "container"}
   [:h2 (str "Add mesurment to experiment #" (get-in @state [:my-experiment :id]))]
   [experiment-info (:my-experiment @state)]
   [add-mesurment state 5]])
