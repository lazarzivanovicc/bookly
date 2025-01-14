(ns bookly.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bookly.core :refer :all]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [bookly.handler :refer :all]
            [midje.sweet :refer :all]))


(fact "Test fetching of user reading list"
      (let [req {:body {}}]
        (generate-reading-list req) =not=> nil))


(fact "Test fetching of collection stats"
      (collection-stats {:identity {:username "LazarZivanovicc"} :body {"collection-id" 1}}) =not=> nil
      (collection-stats {:identity {:username "LazarZivanovicc"} :body {"collection-id" 1}}) => {:status 200,
                                                                                                 :body
                                                                                                 {:user-id 1,
                                                                                                  :books
                                                                                                  '({:id 1,
                                                                                                     :title "The Hobbit",
                                                                                                     :popularity 95,
                                                                                                     :pages 310,
                                                                                                     :genres ["Fantasy"],
                                                                                                     :streak-cost 5,
                                                                                                     :author "J.R.R. Tolkien"}),
                                                                                                  :total-books 1,
                                                                                                  :total-pages 310,
                                                                                                  :average-pages 310,
                                                                                                  :genres #{"Fantasy"}}})


(fact "Test recommendations by genre"
      (recommend-by-genre {:identity {:username "LazarZivanovicc"} :body {"genre" "Fantasy"}}) =not=> nil
      (recommend-by-genre {:identity {:username "LazarZivanovicc"} :body {"genre" "Fantasy"}}) => {:status 200
                                                                                                   :body {:genre "Fantasy"
                                                                                                          :books [{:id 1
                                                                                                                   :title "The Hobbit"
                                                                                                                   :author "J.R.R. Tolkien"
                                                                                                                   :popularity 95}
                                                                                                                  {:id 2
                                                                                                                   :title "Dune"
                                                                                                                   :author "Frank Herbert"
                                                                                                                   :popularity 90}]}}


      (recommend-by-genre {:identity {:username "LazarZivanovicc"} :body {"genre" "Mystery"}}) => {:status 200
                                                                                                   :body {:genre "Mystery"
                                                                                                          :books []}})


(fact "Test recommendations by user's preferred author"
      (recommend-by-author) => {:author "George Orwell", :books ["1984" "Animal Farm"]})


(fact "Test reading reminder generation"
      (create-reading-reminder) => {:book "1984", :reminder-time "Evening", :note "Read for an hour"})


(fact "Test reading progress tracker"
      (track-reading-progress) => {:book "1984", :total-pages 328, :pages-read 164, :progress 50.0})


(fact "Test streak extension"
      (extend-user-streak) => {:streak {:total 11, :claimed-today true}})


(fact "Test Book Review fetch"
      (get-book-reviews) =not=> nil)


(fact "Registration test"
      (let [req-new-user {:body {"username" "JovanZivanovicc"
                                 "first-name" "Jovan"
                                 "last-name" "Zivanovic"
                                 "password" "fakepass"
                                 "favorite-author" "George Orwell"}}
            req-existing-user {:body {"username" "LazarZivanovicc"
                                      "first-name" "Lazar"
                                      "last-name" "Zivanovic"
                                      "password" "fakepass1"
                                      "favorite-author" "George Orwell"}}]
        (register req-new-user) =not=> nil
        (register req-existing-user) =not=> nil
        (register req-existing-user) =>  {:message (str "User "
                                                        (get-in req-existing-user [:body "username"])
                                                        " already exists")}))


(fact "Login test"
      (let [req-valid {:body {"username" "LazarZivanovicc"
                              "password" "fakepass1"}}
            req-invalid-username {:body {"username" "Lazar"
                                         "password" "fakepass1"}}
            req-invalid-password {:body {"username" "Lazar"
                                         "password" "fakepass"}}]
        (login req-valid) =not=> nil
        (login req-valid) => {:message (str (get-in req-valid [:body "username"])
                                            " logged-in successfully")
                              :token (jwt/sign {:username (get-in req-valid [:body "username"])
                                                :exp (time/plus (java.util.Date.) (time/seconds 86400))}
                                               "secret" {:alg :hs512})}
        (login req-invalid-username) => {:message "Invalid login data please try again"}
        (login req-invalid-password) => {:message "Invalid login data please try again"}))


(fact "Test Jaccard Similarity"
      (jaccard-similarity #{1 2 3} #{1 2 3}) =not=> nil
      (jaccard-similarity #{1 2 3} #{5 6 7}) => 0
      (jaccard-similarity #{1 2 3} #{1 2 3}) => 1
      (jaccard-similarity #{1 2 3} #{1 2 5}) => 1/2
      (jaccard-similarity #{1 2 3} #{1 2}) => 2/3
      (jaccard-similarity #{1 2} #{1 2 3}) => 2/3)


(fact "Test Get User's Books"
      (get-user-books 1) =not=> nil
      (get-user-books 1) => #{1 3 5}
      (get-user-books 2) => #{2})


(fact "Find Similar Users"
      (similar-users 1) =not=> nil
      (similar-users 1) => '({:user-id 6, :similarity 1} {:user-id 5, :similarity 1/3})
      (similar-users 2) => '({:user-id 5, :similarity 1/5}))


(fact "Test collaborative recommendations - new version"
      (recommend-books 1) =not=> nil
      (recommend-books 1) => '({:id 7, :title "The Fellowship of the Ring", :popularity 94}
                               {:id 6, :title "Game of Thrones", :popularity 91}
                               {:id 2, :title "Dune", :popularity 90}))

(fact "Test get review sentiment function"
      (get-review-sentiment) =not=> nil
      (get-review-sentiment) => {:status 200,
                                 :body
                                 {:review-sentiment
                                  '({"1984"
                                     [{:user "Anna", :rating 5, :review "Fantastic book!", :sentiment "positive"}
                                      {:user "John", :rating 4, :review "Depressive!", :sentiment "negative"}]})}})


(fact "Test notify users about new books"
      (notify-users-about-new-books) =not=> nil
      (notify-users-about-new-books) => {:users {"LazarZivanovicc" {:favorite-author "J.K. Rowling"}
                                                 "DusanTrunicc" {:favorite-author "George Orwell"}}
                                         :new-books {"J.K. Rowling" ["Harry Potter and the Philosopher's Stone"
                                                                     "Harry Potter and the Chamber of Secrets"]
                                                     "George Orwell" ["1984" "Animal Farm"]}})


(fact "Test leave personal notes"
      (leave-personal-notes 1) =not=> nil
      (leave-personal-notes 1) => {:book "The Hobbit", :note "This book is a masterpiece."})


(fact "Test spend streak to unlock book"
      (spend-streak-to-unlock-book) =not=> nil
      (spend-streak-to-unlock-book) => {:user {:streak {:total 5, :claimed-today true}}, :message "You have successfully unlocked the book"})


(fact "Test set reading goal"
      (set-reading-goal 1 1 "2025-01-15") =not=> nil
      (set-reading-goal 1 1 "2025-01-15") => {:user {:reading-goals [{:book {:id 1, :title "The Hobbit", :popularity 95},
                                                                      :target-date "2025-01-15",
                                                                      :status "in-progress",
                                                                      :created-at (java.util.Date.)}]}})


(fact "Test create collection"
      (create-collection {:identity {:username "LazarZivanovicc"}
                          :body {"name" "Summer Reading 1"
                                 "description" "Books for summer 2025"
                                 "public" true}}) =not=> nil
      (create-collection {:identity {:username "LazarZivanovicc"}
                          :body {"name" "Summer Reading 2"
                                 "description" "Books for summer 2025"
                                 "public" true}}) => {:body {:message "Collection created successfully"
                                                             :collection {:id 4,
                                                                          :user-id 1,
                                                                          :name "Summer Reading 2",
                                                                          :description "Books for summer 2025",
                                                                          :public true,
                                                                          :created-at (java.util.Date.),
                                                                          :updated-at (java.util.Date.)}}
                                                      :status 200})


(fact "Test add book to collection"
      (add-book-to-collection {:identity {:username "LazarZivanovicc"}
                               :body {"user-id" 1
                                      "book-id" 3
                                      "collection-id" 1}}) =not=> nil
      (add-book-to-collection {:identity {:username "LazarZivanovicc"}
                               :body {"user-id" 1
                                      "book-id" 1
                                      "collection-id" 2}}) => {:body {:message "Book added to collection successfully"
                                                                      :user-book {:user-id 1,
                                                                                  :book-id 1,
                                                                                  :collection-id 2,
                                                                                  :status "want-to-read",
                                                                                  :progress nil,
                                                                                  :added-at (java.util.Date.),
                                                                                  :updated-at (java.util.Date.)}}
                                                               :status 200})


(fact "Test subscribe to collection"
      (subscribe-to-collection "LazarZivanovicc" 1) =not=> nil
      (subscribe-to-collection "MarijaArsenijevicc" 1) => [{:collection-id 1, :user-id 2} {:collection-id 1, :user-id 1} {:collection-id 1, :user-id 6}])

;; TODO
;; How can I test my endpoints (app (mock/request :get "/api/collection-stats")) returns error 404? Why? It works in Postman and Browser!
;; Possibly organize tests in groups (facts is used as a container for multiple fact statements), single simple test case should be represented with a fact
;; Do tests run immedietly when I start the ring server?

;; CREATE TEST DB THAT I WILL CREATE SEED AND DESTORY
;; CHECK MIDJE PROVIDED