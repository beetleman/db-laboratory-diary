(ns db-laboratory-diary.form
  (:require [goog.dom.forms :as gforms]
            [db-laboratory-diary.api :as api]
            [accountant.core :as accountant]))

(defn form->map [form]
  (let [form-map (gforms/getFormDataMap form)]
    (reduce
     (fn [result key]
       (let [value (.get form-map key)]
         (if (and value (= 1 (count value)))
           (assoc result (keyword key) (first value))
           (assoc result (keyword key) value))))
     {}
     (.getKeys form-map))))


(defn form-group [label id input]
  [:div.form-group
   [:label {:for id} label]
   input])


(defn form-group-input [label name {:keys [type required lenght]
                                    :or {type ""
                                         lenght 5
                                         required true}
                                    :as params}]
  (let [id (str "input" name)
        title (str  lenght " characters minimum")
        pattern (str ".{" lenght ",}")]
    [form-group label id
     [:input (merge {:class "form-control"
                     :id id
                     :pattern pattern
                     :title title
                     :name name
                     :type type
                     :required required}
                    params)]]))

(defn form-group-select [data {:keys [value text]} label name params]
  (let [id (str "select" name)]
    [form-group label id
     [:select (merge {:class "form-control"
                      :id id
                      :name name}
                     params)
      (for [d data]
        ^{:key (value d)} [:option {:value (value d)} (text d)])]]))


(defn form-checkbox [label name & [params]]
  (let [params (merge {:type "checkbox" :name name}
                      (or params {}))]
    [:div.checkbox
     [:label
      [:input params] label]]))


(defn form-yes-no [cancel-event]
  [:div {:class "btn-group btn-group-sm pull-right" :role "group"}
    [:button {:class "btn btn-warning" :on-click cancel-event}
    "Cancel"]
    [:button {:class "btn btn-success" :type "submit"}
     "Ok"]])


(defn on-add-fn [state state-target api-url redirect-url add-msg add-fn]
  (fn [event]
    (.preventDefault event)
    (let [params (add-fn (form->map (.-target event)))]
      (api/api-post
       state state-target api-url params
       {:handler-fn (fn [a k v]
                      (if-let [error (:error v)]
                        (assoc a :danger-msg error)
                        (do
                          (accountant/navigate! redirect-url)
                          (assoc a :info-msg
                                 add-msg))))}))))
