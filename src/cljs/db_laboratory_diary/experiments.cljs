(ns db-laboratory-diary.experiments
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [accountant.core :as accountant]
            [db-laboratory-diary.date :as date]
            [reagent-forms.core :refer [bind-fields]]
            [reagent.core :as r]))


(defn experiments-table [state]
  [tables/header state "Experiments list"
   [tables/table (map (fn [e]
                        (merge e {:start_date (-> e :start_date date/parse)
                                  :stop_date (-> e :stop_date date/parse)}))
                      (:experiments @state))
    {:start_date "Start"
     :stop_date "Stop"
     :name "Area name"
     :address "Area address"
     :fertilizer "Fertilizer"}
    (fn [data] [:a {:href (str "/experiments/" (:id data))}
                "detail"])]])


(defn on-add-experiment-fn [state]
  (form/on-add-fn state :experiment "experiments" "/experiments" "Experiment added"
                  (fn [experiment] experiment)))


(defn experiment-form [state on-submit on-cancel values]
  [:form {:on-submit on-submit}
   [form/form-group-select (:users @state) {:value :id :text :username} "Manager" "manager_id"
    {:value (:manager_id values)}]
   [form/form-group-select (:users @state) {:value :id :text :username} "Laborants" "laborants_ids"
    {:multiple true
     :value (:laborants_ids values)}]
   [form/form-group-select (:area_data @state) {:value :id :text :address} "Area data" "area_data_id"
    {:value (:area_data_id values)}]
   [form/form-group-input "Start" "start_date" {:type "date" :value (:start_date values)}]
   [form/form-group-input "End" "stop_date" {:type "date" :value (:stop_date values)}]
   [form/form-checkbox "Fertilizer" "fertilizer" {:checked (:fertilizer values)}]
   [form/form-yes-no on-cancel]])

(defn add-experiment-form [state]
  (experiment-form state (on-add-experiment-fn state) (e/on-view state) {}))


(defn edit-experiment-form-cancel [e]
  (.preventDefault e)
  (accountant/navigate! "/experiments"))

(defn edit-experiment-form [state]
  (let [experiment (:experiment @state)]
    (experiment-form state edit-experiment-form-cancel
                     edit-experiment-form-cancel
                     {:manager_id (get-in experiment [:manager :id])
                      :laborants_ids (map :id (get-in experiment [:laborants]))
                      :area_data_id (get-in experiment [:area_data :id])
                      :start_date (-> experiment :start_date date/parse)
                      :stop_date (-> experiment :stop_date date/parse)
                      :fertilizer (:fertilizer experiment)})))


(defn experiment-details [experiment]
  [:dl.dl-horizontal
   [:dt "Manager:"] [:dd (get-in experiment [:manager :username])]
   [:dt "Laborants:"] [:dd (apply str (interpose ", " (map :username (:laborants experiment))))]
   [:dt "Start:"] [:dd (-> experiment :start_date date/parse)]
   [:dt "End:"] [:dd (-> experiment :stop_date date/parse)]
   [:dt "Area Name:"] [:dd (get-in experiment [:area_data :name])]
   [:dt "Area Address:"] [:dd (get-in experiment [:area_data :address])]
   [:dt "Fertilizer:"] [:dd (-> experiment :fertilizer str)]])


(defn experiments-page [state]
  [:div {:class "container"}
   [:h2 "Experiments"]
   (if (= :add (:page-state @state))
     [add-experiment-form state]
     [experiments-table state])])

(defn experiment-page [state]
  [:div {:class "container"}
   [:h2 (str "Experiment #" (get-in @state [:experiment :id]))]
   [experiment-details (:experiment @state)]])
