(ns db-laboratory-diary.login
  (:require [db-laboratory-diary.form :as form]
            [db-laboratory-diary.api :as api]
            [accountant.core :as accountant]))

(defn submit-login [state event]
  (.preventDefault event)
  (let [user (form/form->map (.-target event))]
    (api/api-post
     state :user "check-credencials" user
     {:headers {}
      :handler-fn (fn [a k v]
                    (if (:username v)
                      (do
                        (accountant/navigate! "/")
                        (assoc a k user))
                      (-> a (assoc k nil)
                          (assoc :danger-msg
                                 "Bad login or password"))))})))

(defn login-page [state]
  [:div {:class "container"}
   [:form {:class "form-signin" :on-submit (partial submit-login state)}
    [:h2 {:class "form-signin-heading"} "Please sign in"]
    [:label {:for "inputUsername" :class"sr-only"} "Username"]
    [:input {:id "inputUsername" :name "username"
             :pattern ".{5,}" :required true :title "5 characters minimum"
             :class "form-control" :placeholder "Username"}]
    [:label {:for "inputPassword" :class "sr-only"} "Password"]
    [:input {:type "password" :id "inputPassword" :name "password"
             :pattern ".{5,}" :required true :title "5 characters minimum"
             :class "form-control" :placeholder "Password"}]
    [:button {:class "btn btn-lg btn-primary btn-block" :type "submit"}
     "Sign in"]]])
