(ns db-laboratory-diary.tables)

(defn table-tr [tag data order]
  (into [:tr]
        (map (fn [k] [tag (str (k data))]) order)))

(defn table [data definition]
  (let [order (keys definition)]
    [:table {:class "table"}
     [:thead
      [table-tr :th definition order]]
     (into [:tbody]
           (map (fn [d] [table-tr :td d order]) data))]))
