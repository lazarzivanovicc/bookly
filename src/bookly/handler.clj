(ns bookly.handler
  (:require [compojure.core :refer :all]
            [next.jdbc :as jdbc]
            [bookly.resources :refer [db]]))


;; Checking DB Connection
(jdbc/execute! db ["select * from users"])

;; ===== Registration and Login =====

(defn login
  [request]
  (get-in request [:body "username"]))

;; ===== User Stories =====
;; 1. User Story - Generate list of books to read
(defn generate-reading-list []
  (let [all-books ["1984" "Brave New World" "The Great Gatsby" "Dune" "The Hobbit"]
        to-read-count (rand-int 5)]
    {:to-read (take to-read-count (shuffle all-books))}))


;; 2. User Story - Generate and View Statistics of Book Collection
;; User may have a collection of already read books, a collection of books that he/she is currently reading,
;; a collection he/she intends to read or a collection that he recommends to other users
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


;; 3. User Story - Get Book Recommendations by Genre
(defn recommend-by-genre []
  (let [genres {:Fantasy ["The Hobbit" "Harry Potter" "Game of Thrones" "The Fellowship of the Ring"]
                :Sci-Fi ["Dune" "1984"]
                :Romance ["Pride and Prejudice" "The Notebook" "Five Feet Apart"]}
        selected-genre (rand-nth (keys genres))]
    {:genre selected-genre
     :books (genres selected-genre)}))



;; 4. User Story - Get Book Recommendations by Favorite Author
(defn recommend-by-author []
  (let [user {:preferred-author "George Orwell"}
        authors {"J.K. Rowling" ["Harry Potter and the Philosopher's Stone"
                                 "Harry Potter and the Chamber of Secrets"]
                 "George Orwell" ["1984" "Animal Farm"]}
        selected-author (first (filter #(= (:preferred-author user) %) (keys authors)))]
    {:author selected-author
     :books (authors selected-author)}))


;; 5. User Story - User Generates Reading Reminder
(defn create-reading-reminder []
  (let [books ["1984" "Dune" "The Great Gatsby" "The Hobbit"]
        times ["Morning" "Evening"]
        notes ["Read for 30 minutes", "Read for an hour"]]
    {:book (first books)
     :reminder-time (second times)
     :note (second notes)}))


;; 6. User Story - Track Reading Progress of a User 
(defn track-reading-progress []
  (let [reading-log {:book "1984" :total-pages 328 :pages-read 164}]
    (assoc reading-log :progress (double (* 100 (/ (:pages-read reading-log) (:total-pages reading-log)))))))


;; 7. User Story - Leave Personal Notes About a Book


;; 8. User Story - User wants to See Book Reviews
(defn get-book-reviews []
  (let [reviews {"1984" [{:user "Anna" :rating 5 :review "Fantastic book!"}
                         {:user "John" :rating 4 :review "Depresive!"}]
                 "The Hobbit" [{:user "Steve" :rating 5 :review "Pure magic!"}
                               {:user "Nicolas" :rating 4 :review "Great but little bit too slow."}]}
        selected-book (rand-nth (keys reviews))]
    {:book selected-book
     :reviews (reviews selected-book)}))


;; 9. User Story - User Wants to See Overall Sentiment of the Book Reviews
;; I will use https://huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest to get sentiment of each individual review
;; And I will have total_positive/total_negative/total_neutral by book 


;; 10. User Story - Notify Users About New Books by Their Favorite Author


;; 11. User Story - User Subscribes to Someone's Book List - they will get the notification


;; 12. User Story - User Extends his/her Reading Streak by checking-in for a Reading Session  
(defn extend-user-streak []
  (let [user {:streak {:total 10 :claimed-today false}}]
    (if (not (get-in user [:streak :claimed-today]))
      (assoc-in (assoc-in user [:streak :claimed-today] true) [:streak :total] (inc (get-in user [:streak :total])))
      user)))

;; 13. User Story - User Spends his Streak to buy a Book

;; 14. User Story - User Sets and Tracks Personal Reading Goals

;; 15. User Story - User Receives Notifications for Book Deals
;; I as a User, I want to receive notifications about discounts or deals on books in my wish list or by my favorite authors.

