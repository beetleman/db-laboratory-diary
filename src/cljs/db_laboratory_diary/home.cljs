(ns db-laboratory-diary.home)

(defn home-page [state]
  [:div {:class "container"}
   [:h2 "Welcome to db-laboratory-diary"]
   [:div [:a {:href "/about"} "go to about page"]]])
