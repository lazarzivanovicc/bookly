(ns bookly.http-helpers)

;; TODO - Add more helpers
(defn response-ok
  "Returns a 200 OK response with a given message or body."
  [body]
  {:status 200
   :body body})


(defn response-created
  "Returns a 201 Created response with a given body."
  [body]
  {:status 201
   :body body})


(defn response-unauthorized
  "Returns a 401 Unauthorized response with an error message."
  [error-message]
  {:status 401
   :body {:error error-message}})


(defn response-forbidden
  "Returns a 403 Forbidden response with an error message."
  [error-message]
  {:status 403
   :body {:error error-message}})


(defn response-server-error
  "Returns a 500 Error response with an error message."
  [error-message]
  {:status 500
   :body {:error error-message}})


(defn response-error
  "Returns a Error response with assigned status and error message."
  [status error-message]
  {:status status
   :body {:error error-message}})