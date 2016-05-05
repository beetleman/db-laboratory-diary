(ns db-laboratory-diary.date
  (:require [cljs-time.core :as time]
            [cljs-time.coerce :as time-coerce]
            [cljs-time.format :as time-format]))


(defn parse [s]
  (if (nil? s)
    nil
    (let [fmt (time-format/formatter "yyyy-MM-dd")]
      (->> s
           time-format/parse
           (time-format/unparse fmt)))))


(defn create_data->date [create_data]
  (let [t (time-coerce/from-long create_data)
        fmt (time-format/formatter "yyyy-MM-dd")]
    (time-format/unparse fmt t)))
