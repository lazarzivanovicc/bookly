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
      (collection-stats {:identity {:username "LazarZivanovicc"}
                         :body {"collection-id" 1}}) =not=> nil
      (collection-stats {:identity {:username "LazarZivanovicc"}
                         :body {"collection-id" 1}}) => {:status 200
                                                         :body
                                                         {:user-id 1
                                                          :books '({:id 1
                                                                    :title "The Hobbit"
                                                                    :popularity 95
                                                                    :pages 310
                                                                    :genres ["Fantasy"]
                                                                    :streak-cost 5
                                                                    :author "J.R.R. Tolkien"})
                                                          :total-books 1
                                                          :total-pages 310
                                                          :average-pages 310
                                                          :genres #{"Fantasy"}}})


(fact "Test recommendations by genre"
      (recommend-by-genre {:identity {:username "LazarZivanovicc"}
                           :body {"genre" "Fantasy"}}) =not=> nil
      (recommend-by-genre {:identity {:username "LazarZivanovicc"}
                           :body {"genre" "Fantasy"}}) => {:status 200
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
      (recommend-by-author {:identity {:username "LazarZivanovicc"} :body {}}) =not=> nil
      (recommend-by-author {:identity {:username "LazarZivanovicc"} :body {}}) => {:status 200
                                                                                   :body {:author "J.K. Rowling"
                                                                                          :books ()}})


(fact "Test reading reminder generation"
      (create-reading-reminder {:identity {:username "LazarZivanovicc"}
                                :body {"book-id" 1
                                       "reminder-time" "Evening"
                                       "note" "Read for an hour"}}) =not=> nil
      (create-reading-reminder {:identity {:username "LazarZivanovicc"}
                                :body {"book-id" 1
                                       "reminder-time" "Evening"
                                       "note" "Read for an hour"}}) => {:status 200
                                                                        :body {:user-id 1
                                                                               :book-id 1
                                                                               :reminder-time "Evening"
                                                                               :note "Read for an hour"
                                                                               :pages-left 146}})


(fact "Test reading progress tracker"
      (track-reading-progress {:identity {:username "LazarZivanovicc"}
                               :body {"book-id" 1}}) =not=> nil
      (track-reading-progress {:identity {:username "LazarZivanovicc"}
                               :body {"book-id" 1}}) => {:status 200
                                                         :body {:book "The Hobbit"
                                                                :total-pages 310
                                                                :pages-read 164
                                                                :progress 52.90322580645161
                                                                :pages-left 146}})


(fact "Test streak extension"
      (extend-user-streak {:identity {:username "MarijaArsenovic"} :body {}}) =not=> nil
      (extend-user-streak {:identity {:username "LazarZivanovicc"} :body {}}) => {:status 200
                                                                                  :body {:message "Streak extended!"
                                                                                         :streak {:total 11
                                                                                                  :claimed-today true}}})


(fact "Test Book Review fetch"
      (get-book-reviews 1) =not=> nil
      (get-book-reviews 1) => {:reviews [{:id 1
                                          :user-id 1
                                          :book-id 1
                                          :rating 5
                                          :review "Fantastic book!"
                                          :created-at (java.util.Date. 1234567890000)
                                          :updated-at (java.util.Date. 1234567890000)}
                                         {:id 2
                                          :user-id 2
                                          :book-id 1
                                          :rating 4
                                          :review "Masterpiece"
                                          :created-at (java.util.Date. 1234567890000)
                                          :updated-at (java.util.Date. 1234567890000)}]})


(fact "Test Book Review"
      (book-reviews {:identity {:username "LazarZivanovicc"}
                     :body {"book-id" 1}}) =not=> nil
      (book-reviews {:identity {:username "LazarZivanovicc"}
                     :body {"book-id" 1}}) => {:status 200
                                               :body {:reviews [{:id 1
                                                                 :user-id 1
                                                                 :book-id 1
                                                                 :rating 5
                                                                 :review "Fantastic book!"
                                                                 :created-at (java.util.Date. 1234567890000)
                                                                 :updated-at (java.util.Date. 1234567890000)}
                                                                {:id 2
                                                                 :user-id 2
                                                                 :book-id 1
                                                                 :rating 4
                                                                 :review "Masterpiece"
                                                                 :created-at (java.util.Date. 1234567890000)
                                                                 :updated-at (java.util.Date. 1234567890000)}]}})


(fact "Analyze sentiment"
      (analyze-sentiment "You are amazing!") =not=> nil
      (analyze-sentiment "You are amazing") => {:label "positive"}
      (analyze-sentiment "You are terrible!") => {:label "negative"}
      (analyze-sentiment "How are you?") => {:label "neutral"})


(fact "Get reviews and their sentiment"
      (get-review-sentiment {:identity {:username "LazarZivanovicc"}
                             :body {"book-id" 1}}) =not=> nil
      (get-review-sentiment {:identity {:username "LazarZivanovicc"}
                             :body {"book-id" 1}}) => {:status 200,
                                                       :body
                                                       {:review-sentiment
                                                        [{:id 1,
                                                          :user-id 1,
                                                          :book-id 1,
                                                          :rating 5,
                                                          :review "Fantastic book!",
                                                          :created-at (java.util.Date. 1234567890000),
                                                          :updated-at (java.util.Date. 1234567890000),
                                                          :sentiment "positive"}
                                                         {:id 2,
                                                          :user-id 2,
                                                          :book-id 1,
                                                          :rating 4,
                                                          :review "Masterpiece",
                                                          :created-at (java.util.Date. 1234567890000),
                                                          :updated-at (java.util.Date. 1234567890000),
                                                          :sentiment "neutral"}],
                                                        :totals {:positive 1, :negative 0, :neutral 0}}})


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
      (recommend-books 1) => '({:id 2
                                :title "Dune"
                                :popularity 90
                                :pages 412
                                :genres ["Fantasy"]
                                :streak-cost 5
                                :author "Frank Herbert"}))



(fact "Test leave personal notes"
      (leave-personal-note {:identity {:username "LazarZivanovicc"}
                            :body {"book-id" 2
                                   "note" "This book is a great read."}}) =not=> nil
      (leave-personal-note {:identity {:username "LazarZivanovicc"}
                            :body {"book-id" 1
                                   "note" "This book is a great read."}}) => {:status 200
                                                                              :body {:message "Note added successfully"
                                                                                     :note-id 3
                                                                                     :note "This book is a great read."}})


(fact "Test spend streak to unlock book"
      (spend-streak-to-unlock-book {:identity {:username "LazarZivanovicc"} :body {"book-id" 6}}) =not=> nil
      (spend-streak-to-unlock-book {:identity {:username "LazarZivanovicc"} :body {"book-id" 7}}) => {:status 200
                                                                                                      :body {:user-id 1
                                                                                                             :streak {:total 0
                                                                                                                      :claimed-today false}
                                                                                                             :message "You have successfully unlocked the book"}}
      (spend-streak-to-unlock-book {:identity {:username "LazarZivanovicc"} :body {"book-id" 5}}) => {:body {:error "You don't have enough streak to unlock this book"}
                                                                                                      :status 500})


(fact "Test set reading goal"
      (set-reading-goal {:identity {:username "LazarZivanovicc"} :body {"book-id" 6 "target-date" "2025-05-18"}}) =not=> nil
      (set-reading-goal {:identity {:username "LazarZivanovicc"} :body {"book-id" 7 "target-date" "2025-05-18"}}) => {:status 200
                                                                                                                      :body {:message "Reading goal set successfully."}})


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
      (subscribe-to-collection {:identity {:username "LazarZivanovicc"}
                                :body {"collection-id" 1}}) =not=> nil
      (subscribe-to-collection {:identity {:username "MarijaArsenijevicc"}
                                :body {"collection-id" 1}}) => {:body {:new-subscription {:collection-id 1 :user-id 6}} :status 200})


(fact "Test dot product function"
      (dot [1 1] [1 1]) =not=> nil
      (dot [1 1] [1 2]) => 3
      (dot [1 1 1] [1 2 3]) => 6
      (dot [1 2] [1]) => (throws RuntimeException "Vectors must have the same number of elements"))


(fact "Test Euclidian norm function"
      (euclidian-norm [1 1 1 1]) =not=> nil
      (euclidian-norm [1 1 1 1]) => 2.0
      (euclidian-norm [3 4]) => 5.0)


(fact "Test Cosine Similarity function"
      (cosine-similarity [1 1 1] [1 1 1]) =not=> nil
      (cosine-similarity [1000 100 55 47] [1000 100 55 47]) => 1.00
      (cosine-similarity [-1 0] [1 0]) => -1.00
      (cosine-similarity [1 0] [0 1]) => 0.00)
