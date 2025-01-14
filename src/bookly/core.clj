(ns bookly.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as ring-response]
            [bookly.handler :refer :all]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [dotenv :refer [env]]))


(def auth-backend (jws-backend {:secret (env "SECRET_KEY") :options {:alg :hs512}}))

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
  (POST "/api/register" req (ring-response/response (register req)))
  (POST "/api/login" req (ring-response/response (login req)))
  (GET "/api/reading-list" req (ring-response/response (generate-reading-list req)))
  (GET "/api/collection-stats" req (ring-response/response (collection-stats req)))
  (POST "/api/recommendations" req [] (ring-response/response (recommend-books req)))
  (GET "/api/recommendations-by-genre" req (ring-response/response (recommend-by-genre req)))
  (GET "/api/recommendations-by-author" [] (ring-response/response (recommend-by-author)))
  (GET "/api/create-reading-reminder" [] (ring-response/response (create-reading-reminder)))
  (GET "/api/track-reading-progress" [] (ring-response/response (track-reading-progress)))
  (GET "/api/extend-user-streak" [] (ring-response/response (extend-user-streak)))
  (GET "/api/get-book-reviews" [] (ring-response/response (get-book-reviews)))
  (POST "/api/collections" req (ring-response/response (create-collection req)))
  (POST "/api/collections/add-book" req (ring-response/response (add-book-to-collection req)))
  (route/not-found "Not Found"))



(def app
  (-> app-routes
      (wrap-default-content-type)
      (wrap-defaults api-defaults)
      (ring-json/wrap-json-body)
      (ring-json/wrap-json-response)
      (wrap-authentication auth-backend)))