(ns bookly.handler
  (:require [compojure.core :refer :all]))


;; 1. User Story - Generate list of books to read
(defn generate-reading-list []
  (let [all-books ["1984" "Brave New World" "The Great Gatsby" "Dune" "The Hobbit"]
        to-read-count (rand-int 5)]
    {:to-read (take to-read-count (shuffle all-books))}))


;; 2. User Story - Generate and View Statistics of Book Collection
(defn collection-stats []
  (let [books [{:title "1984" :pages 328 :genres ["Fantasy", "Sci-Fi"]}
               {:title "The Hobbit" :pages 310 :genres ["Fantasy", "Sci-Fi"]}
               {:title "War and Peace" :pages 1225 :genres ["Fantasy", "Sci-Fi"]}
               {:title "The Great Gatsby" :pages 180 :genres ["Fantasy", "Sci-Fi"]}
               {:title "Dune" :pages 412 :genres ["Fantasy", "Sci-Fi"]}]
        total-books (count books)
        total-pages (reduce + (map :pages books))
        all-genres (set (flatten (map :genres books)))]
    {:total-books total-books
     :total-pages total-pages
     :average-pages (/ total-pages total-books)
     :genres all-genres}))


