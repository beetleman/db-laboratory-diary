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
