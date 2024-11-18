(ns bookly.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as ring-response]
            [bookly.handler :refer :all]))


(defn wrap-default-content-type
  "Middleware that appends Content-Type application/json as default"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (nil? (get-in response [:headers "Content-Type"]))
        (assoc-in response [:headers "Content-Type"] "application/json")
        response))))


(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/api/reading-list" [] (ring-response/response (generate-reading-list)))
  (route/not-found "Not Found"))



(def app
  (-> app-routes
      (wrap-default-content-type)
      (wrap-defaults site-defaults)
      (ring-json/wrap-json-response)))