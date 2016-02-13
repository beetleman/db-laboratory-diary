(ns db-laboratory-diary.experiments
  (:require [db-laboratory-diary.tables :as tables]
            [db-laboratory-diary.events :as e]
            [db-laboratory-diary.form :as form]
            [db-laboratory-diary.api :as api]
            [reagent-forms.core :refer [bind-fields]]
            [accountant.core :as accountant]
            [reagent.core :as r]))



(defn experiments-page [state]
  [:div {:class "container"}
   [:h2 "Experiments"]])
