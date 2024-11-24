(ns bookly.resources
  (:require [next.jdbc :as jdbc]))

(def db-config
  {:dbtype "postgresql"
   :dbname "bookly"
   :host "localhost"
   :user "postgres"
   :password "root"})

(def db (jdbc/get-datasource db-config))