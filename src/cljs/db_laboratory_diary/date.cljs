(ns db-laboratory-diary.date
  (:require [cljs-time.core :as time]
            [cljs-time.format :as time-format]))


(defn parse [s]
  (if (nil? s)
    nil
    (let [fmt (time-format/formatter "yyyy-MM-dd")]
      (->> s
           time-format/parse
           (time-format/unparse fmt)))))
