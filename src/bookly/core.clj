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
  (GET "/api/collection-stats" [] (ring-response/response (collection-stats)))
  (GET "/api/recommendations-by-genre" [] (ring-response/response (recommend-by-genre)))
  (GET "/api/recommendations-by-author" [] (ring-response/response (recommend-by-author)))
  (GET "/api/create-reading-reminder" [] (ring-response/response (create-reading-reminder)))
  (GET "/api/track-reading-progress" [] (ring-response/response (track-reading-progress)))
  (GET "/api/extend-user-streak" [] (ring-response/response (extend-user-streak)))
  (GET "/api/get-book-reviews" [] (ring-response/response (get-book-reviews)))
  (route/not-found "Not Found"))



(def app
  (-> app-routes
      (wrap-default-content-type)
      (wrap-defaults site-defaults)
      (ring-json/wrap-json-response)))