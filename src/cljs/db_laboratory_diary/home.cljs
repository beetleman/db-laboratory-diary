(ns db-laboratory-diary.home)

(defn home-page [state]
  [:div {:class "container text-center"}
   [:h1 "Welcome to db-laboratory-diary"]
   [:div [:img {:src "images/flowers.svg"}]]])
