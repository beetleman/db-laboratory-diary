(ns db-laboratory-diary.about)

(defn about-page [state]
  [:div {:class "container"}
   [:h2 "About"]
   [:ul
    [:li "Name: " (get-in @state [:about :name])]
    [:li "Version: " (get-in @state [:about :version])]
    [:li "Tables:"
     (into [:ul ]
           (map (fn [d] [:li (:table_name d)])
                (get-in @state [:about :tables])))]]
   [:div [:a {:href "/"} "go to the home page"]]])
