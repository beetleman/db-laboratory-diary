(ns db-laboratory-diary.form
  (:require [goog.dom.forms :as gforms]))

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

(defn form-group-input [label name {:keys [type]
                              :or {type ""}
                              :as params}]
  (let [id (str "input" name)]
    [:div.form-group
     [:label {:for id} label]
     [:input (merge {:class "form-control"
                     :id id
                     :name name
                     :type type}
                    params)]]))

(defn form-checkbox [label name]
  [:div.checkbox
   [:label
    [:input {:type "checkbox" :name name}] label]])

(defn form-yes-no [cancel-event]
  [:div {:class "btn-group btn-group-sm pull-right" :role "group"}
    [:button {:class "btn btn-warning" :on-click cancel-event}
    "Cancel"]
    [:button {:class "btn btn-success" :type "submit"}
     "Ok"]])
