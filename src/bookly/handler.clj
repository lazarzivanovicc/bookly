(ns bookly.handler
  (:require
   [bookly.http-helpers :refer :all]
   [clj-http.client :as http]
   [cheshire.core :as json]
   [bookly.resources :refer [db]]
   [buddy.auth :refer [authenticated?]]
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]
   [clojure.set :as set]
   [compojure.core :refer :all]
   [dotenv :refer [env]]
   [next.jdbc :as jdbc]))

;; Checking DB Connection
(jdbc/execute! db ["select * from users"])

(def huggingface-endpoint
  "https://api-inference.huggingface.co/models/cardiffnlp/twitter-roberta-base-sentiment-latest")


(def huggingface-token (env "HF_TOKEN"))

;; ----------------------------------------------------------------------------
;; Registration & Login
;; ----------------------------------------------------------------------------
(def users (atom {"LazarZivanovicc"
                  {:id 1
                   :first-name "Lazar",
                   :last-name "Zivanovic",
                   :username "LazarZivanovicc",
                   :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"}
                  "DusanTrunicc"
                  {:id 2
                   :first-name "Dusan",
                   :last-name "Trunic",
                   :username "DusanTrunicc",
                   :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"}
                  "AnaDimitricc"
                  {:id 5
                   :first-name "Ana",
                   :last-name "Dimitric",
                   :username "AnaDimitricc",
                   :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"}
                  "MarijaArsenijevicc"
                  {:id 6
                   :first-name "Marija"
                   :last-name "Arsenijevic"
                   :username "MarijaArsenijevicc"
                   :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"}}))


(def books (atom [{:id 1 :title "The Hobbit" :popularity 95}
                  {:id 2 :title "Dune" :popularity 90}
                  {:id 3 :title "The Great Gatsby" :popularity 85}
                  {:id 4 :title "1984" :popularity 80}
                  {:id 5 :title "War and Peace" :popularity 75}
                  {:id 6 :title "Game of Thrones" :popularity 91}
                  {:id 7 :title "The Fellowship of the Ring" :popularity 94}]))

(def user-book (atom [{:user-id 1 :book-id 1}
                      {:user-id 1 :book-id 3}
                      {:user-id 1 :book-id 5}
                      {:user-id 5 :book-id 1}
                      {:user-id 5 :book-id 3}
                      {:user-id 5 :book-id 2}
                      {:user-id 5 :book-id 7}
                      {:user-id 5 :book-id 6}
                      {:user-id 6 :book-id 1}
                      {:user-id 6 :book-id 3}
                      {:user-id 6 :book-id 5}
                      {:user-id 2 :book-id 2}]))


(def secret (env "SECRET_KEY"))


;; TODO - Use http helper functions
;; TODO - Try honeysql
;; TODO - Add role support - (:identity req) returns claims map so I can pack the role inside claims when creating JWT
;; And check if the function can be exec by the user with a give role

;; TODO - Check what kind of request or exception should I send if user already exists
(defn register
  [request]
  (let [first-name (get-in request [:body "first-name"])
        last-name (get-in request [:body "last-name"])
        username (get-in request [:body "username"])
        password (get-in request [:body "password"])]
    (if (contains? @users username)
      {:message (str "User " username " already exists")}
      (do
        (swap! users assoc username {:first-name first-name
                                     :last-name last-name
                                     :username username
                                     :password (hashers/derive password)
                                     :created-at (time/now)
                                     :updated-at (time/now)})
        {:message (str "User " username " created successfully")}))))


(defn login
  [request]
  (let [username (get-in request [:body "username"])
        password (get-in request [:body "password"])]
    (if (contains? @users username)
      (if (:valid (hashers/verify password (:password (get @users username))))
        (let [claims {:username username
                      :exp (time/plus (time/now) (time/seconds 86400))}
              token  (jwt/sign claims secret {:alg :hs512})]
          {:message (str username " logged-in successfully") :token token})
        {:message "Invalid login data please try again"})
      {:message "Invalid login data please try again"})))


;; ===== User Stories =====

;; ----------------------------------------------------------------------------
;; 1. User Story - Generate list of books to read
;; ----------------------------------------------------------------------------
(defn generate-reading-list
  [req]
  (if (authenticated? req)
    (let [all-books ["1984" "Brave New World" "The Great Gatsby" "Dune" "The Hobbit"]
          to-read-count (rand-int 5)]
      (response-ok {:to-read (take to-read-count (shuffle all-books))}))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 2. User Story - Generate and View Statistics of Book Collection
;; ----------------------------------------------------------------------------
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

;; ----------------------------------------------------------------------------
;; 3. User Story - Get Book Recommendations by Genre
;; ----------------------------------------------------------------------------
(defn recommend-by-genre []
  (let [genres {:Fantasy ["The Hobbit" "Harry Potter" "Game of Thrones" "The Fellowship of the Ring"]
                :Sci-Fi ["Dune" "1984"]
                :Romance ["Pride and Prejudice" "The Notebook" "Five Feet Apart"]}
        selected-genre (rand-nth (keys genres))]
    {:genre selected-genre
     :books (genres selected-genre)}))

;; ----------------------------------------------------------------------------
;; 4. User Story - Get Book Recommendations by Favorite Author
;; ----------------------------------------------------------------------------
(defn recommend-by-author []
  (let [user {:preferred-author "George Orwell"}
        authors {"J.K. Rowling" ["Harry Potter and the Philosopher's Stone"
                                 "Harry Potter and the Chamber of Secrets"]
                 "George Orwell" ["1984" "Animal Farm"]}
        selected-author (first (filter #(= (:preferred-author user) %) (keys authors)))]
    {:author selected-author
     :books (authors selected-author)}))

;; ----------------------------------------------------------------------------
;; 5. User Story - User Generates Reading Reminder
;; ----------------------------------------------------------------------------
(defn create-reading-reminder []
  (let [books ["1984" "Dune" "The Great Gatsby" "The Hobbit"]
        times ["Morning" "Evening"]
        notes ["Read for 30 minutes", "Read for an hour"]]
    {:book (first books)
     :reminder-time (second times)
     :note (second notes)}))

;; ----------------------------------------------------------------------------
;; 6. User Story - Track Reading Progress of a User
;; ---------------------------------------------------------------------------- 
(defn track-reading-progress []
  (let [reading-log {:book "1984" :total-pages 328 :pages-read 164}]
    (assoc reading-log :progress (double (* 100 (/ (:pages-read reading-log) (:total-pages reading-log)))))))

;; ----------------------------------------------------------------------------
;; 7. User Story - Leave Personal Note About a Book
;; ----------------------------------------------------------------------------
(defn leave-personal-notes
  [book-id]
  (let [selected-book (first (filter #(= (:id %) book-id) @books))
        note "This book is a masterpiece."]
    {:book (:title selected-book)
     :note note}))

;; ----------------------------------------------------------------------------
;; 8. User Story - User wants to See Book Reviews
;; ----------------------------------------------------------------------------
(defn get-book-reviews []
  (let [reviews {"1984" [{:user "Anna" :rating 5 :review "Fantastic book!"}
                         {:user "John" :rating 4 :review "Depresive!"}]
                 "The Hobbit" [{:user "Steve" :rating 5 :review "Pure magic!"}
                               {:user "Nicolas" :rating 4 :review "Great but little bit too slow."}]}
        selected-book (rand-nth (keys reviews))]
    {:book selected-book
     :reviews (reviews selected-book)}))

;; ----------------------------------------------------------------------------
;; 9. User Story - User Wants to See Overall Sentiment of the Book Reviews
;; ----------------------------------------------------------------------------

(defn analyze-sentiment
  [text]
  (let [payload (json/encode {:inputs text})
        response (http/post huggingface-endpoint
                            {:headers {"Authorization" (str "Bearer " huggingface-token)
                                       "Content-Type"  "application/json"}
                             :body    payload
                             :as      :json})
        sentiment-vector (first (:body response))
        sentiment (apply max-key :score sentiment-vector)]
    sentiment))


(defn get-review-sentiment
  []
  (let [reviews {"1984" [{:user "Anna" :rating 5 :review "Fantastic book!"}
                         {:user "John" :rating 4 :review "Depressive!"}]}

        review-sentiment (map (fn [[book revs]]
                                {book (mapv (fn [r]
                                              (assoc r :sentiment (:label (analyze-sentiment (:review r)))))
                                            revs)}) reviews)]
    (response-ok
     {:review-sentiment review-sentiment})))

;; ----------------------------------------------------------------------------
;; 10. User Story - Notify Users About New Books by Their Favorite Author
;; ----------------------------------------------------------------------------
(defn notify-users-about-new-books []
  (let [users {"LazarZivanovicc" {:favorite-author "J.K. Rowling"}
               "DusanTrunicc" {:favorite-author "George Orwell"}}
        new-books {"J.K. Rowling" ["Harry Potter and the Philosopher's Stone"
                                   "Harry Potter and the Chamber of Secrets"]
                   "George Orwell" ["1984" "Animal Farm"]}]
    {:users users
     :new-books new-books}))

;; ----------------------------------------------------------------------------
;; 11. User Story - User Subscribes to Someone's Book List - they will get the notification
;; ----------------------------------------------------------------------------

;; ----------------------------------------------------------------------------
;; 12. User Story - User Extends his/her Reading Streak by checking-in for a Reading Session  
;; ----------------------------------------------------------------------------
(defn extend-user-streak []
  (let [user {:streak {:total 10 :claimed-today false}}]
    (if (not (get-in user [:streak :claimed-today]))
      (assoc-in (assoc-in user [:streak :claimed-today] true) [:streak :total] (inc (get-in user [:streak :total])))
      user)))

;; ----------------------------------------------------------------------------
;; 13. User Story - User Spends his Streak to unlock a Book
;; ----------------------------------------------------------------------------
(defn spend-streak-to-unlock-book []
  (let [user {:streak {:total 10 :claimed-today true}}
        book {:id 1 :title "The Hobbit" :streak-cost 5}]
    (if (>= (get-in user [:streak :total]) (:streak-cost book))
      {:user (assoc-in user [:streak :total] (- (get-in user [:streak :total]) (:streak-cost book)))
       :message "You have successfully unlocked the book"}
      {:user user
       :message "You don't have enough streak to unlock this book"})))

;; ----------------------------------------------------------------------------
;; 14. User Story - User Sets and Tracks Personal Reading Goals
;; ----------------------------------------------------------------------------
(defn set-reading-goal
  [user-id book-id target-date]
  (let [user (first (filter #(= (:id %) user-id) @users))
        book (first (filter #(= (:id %) book-id) @books))
        current-goals (get-in user [:reading-goals] [])
        new-goal {:book book
                  :target-date target-date
                  :status "in-progress"
                  :created-at (java.util.Date.)}]
    {:user (assoc user :reading-goals (conj current-goals new-goal))}))

;; ----------------------------------------------------------------------------
;; 15. User Story - User Receives Notifications for Book Deals
;; ----------------------------------------------------------------------------
;; I as a User, I want to receive notifications about discounts or deals on books in my wish list or by my favorite authors.

;; ----------------------------------------------------------------------------
;; 16. User Story - User Gets Recommendation from Users that have similar interest like him (Collaborative Filtering)
;; ----------------------------------------------------------------------------
;; Currently I return the most popular books that user has not read yet

(defn jaccard-similarity
  [set-a set-b]
  (let [intersection (count (set/intersection set-a set-b))
        union (count (clojure.set/union set-a set-b))]
    (if (zero? union) 0 (/ intersection union))))


(defn user-exists?
  [user-id]
  (some #(= (:id %) user-id) (vals @users)))


(defn get-user-books
  [user-id]
  (if (user-exists? user-id)
    (set (map :book-id (filter #(= (:user-id %) user-id) @user-book)))
    (throw (ex-info "User not found"
                    {:type :user-not-found}))))


(defn similar-users
  [user-id]
  (let [user-books (get-user-books user-id)
        users-rest (remove #(= user-id %) (distinct (map :user-id @user-book)))]
    (sort-by :similarity > (filter #(< 0 (:similarity %))
                                   (map (fn [other-user-id]
                                          {:user-id other-user-id
                                           :similarity (jaccard-similarity user-books
                                                                           (get-user-books other-user-id))}) users-rest)))))

(defn recommend-books
  [user-id]
  (let [user-books (get-user-books user-id)
        similar-users-ids (map :user-id (similar-users user-id))
        recommended-books (remove #(contains? user-books %) (mapcat get-user-books similar-users-ids))]
    (sort-by :popularity > (filter #(contains? (set recommended-books) (:id %)) @books))))

