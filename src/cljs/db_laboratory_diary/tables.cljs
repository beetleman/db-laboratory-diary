(ns db-laboratory-diary.tables
  (:require [db-laboratory-diary.events :as e]))

(defn table-tr [tag data order actions]
  (-> {:actions actions} clj->js js/console.debug)
  (into [:tr]
        (into (mapv (fn [k] [tag (str (k data))]) order)
              (mapv (fn [a] [tag [a data]]) actions))))

(defn table [data definition & actions]
  (let [order (keys definition)]
    [:table {:class "table"}
     [:thead
      [table-tr :th definition order]]
     (into [:tbody]
           (mapv (fn [d] [table-tr :td d order actions]) data))]))



(defn header [state title table]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"}
    title
    [:div.pull-right
     [:button {:class "btn btn-success btn-xs" :on-click (e/on-add state)}
      "Add"]]]
   table])
