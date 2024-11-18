(ns bookly.handler
  (:require [compojure.core :refer :all]))


;; 1. User Story - Generate list of books to read
(defn generate-reading-list []
  (let [all-books ["1984" "Brave New World" "The Great Gatsby" "Dune" "The Hobbit"]
        to-read-count (rand-int 5)]
    {:to-read (take to-read-count (shuffle all-books))}))



