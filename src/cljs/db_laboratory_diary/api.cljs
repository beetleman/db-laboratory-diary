(ns db-laboratory-diary.api
  (:require [ajax.core :refer [GET POST]]))

(def api "/api")

(defn api-url [url] (str api "/" url))

(defn authorization-header [{:keys [user]}]
  (if user
    {:authorization (str "Basic " (js/btoa
                                   (str
                                    (:username user)
                                    ":"
                                    (:password user))))}
    {}))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(defn api-get [state state-target url {:keys [] :as extra}]
  (let [extra (merge {:headers (authorization-header @state)
                      :error-handler error-handler
                      :handler-fn assoc}
                     extra)]
    (GET
     (api-url url)
     {:keywords? true
      :response-format :json
      :headers (:headers extra)
      :error-handler (:error-handler extra)
      :handler (fn [response]
                 (swap! state
                        (:handler-fn extra)
                        state-target
                        response))})))

(defn api-post [state state-target url params {:keys [] :as extra}]
  (let [extra (merge {:headers (authorization-header @state)
                      :error-handler error-handler
                      :handler-fn assoc}
                     extra)]
    (POST
     (api-url url)
     {:params params
      :keywords? true
      :format :raw
      :response-format :json
      :headers (:headers extra)
      :error-handler (:error-handler extra)
      :handler (fn [response]
                 (swap! state
                        (:handler-fn extra)
                        state-target
                        response))})))
