(ns bookly.resources
  (:require [next.jdbc :as jdbc]
            [dotenv :refer [env]]))

(def db-config
  {:dbtype (env "DB_TYPE")
   :dbname (env "DB_NAME")
   :host (env "DB_HOST")
   :user (env "DB_USER")
   :password (env "DB_PASS")})

(def db (jdbc/get-datasource db-config))