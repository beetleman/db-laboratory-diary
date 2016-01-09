(ns db-laboratory-diary.events)

(defn on-add [state]
  (fn [e]
    (.preventDefault e)
    (swap! state assoc :page-state :add)))

(defn on-view [state]
  (fn [e]
    (.preventDefault e)
    (swap! state assoc :page-state :view)))

(defn on-delete [state]
  (fn [e]
    (.preventDefault e)
    (swap! state assoc :page-state :delete)))

(defn on-edit [state]
  (fn [e]
    (.preventDefault e)
    (swap! state assoc :page-state :edit)))
