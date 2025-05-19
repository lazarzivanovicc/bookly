(ns bookly.data)

(def users (atom {"LazarZivanovicc" {:id 1
                                     :first-name "Lazar"
                                     :last-name "Zivanovic"
                                     :username "LazarZivanovicc"
                                     :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"
                                     :created-at (java.util.Date.)
                                     :updated-at (java.util.Date.)
                                     :favorite-author "J.K. Rowling"
                                     :streak {:total 10 :claimed-today false}
                                     :reading-goals []}
                  "DusanTrunicc" {:id 2
                                  :first-name "Dusan",
                                  :last-name "Trunic",
                                  :username "DusanTrunicc",
                                  :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"
                                  :created-at (java.util.Date.)
                                  :updated-at (java.util.Date.)
                                  :favorite-author "George Orwell"
                                  :streak {:total 10 :claimed-today false}
                                  :reading-goals []}
                  "AnaDimitricc" {:id 5
                                  :first-name "Ana",
                                  :last-name "Dimitric",
                                  :username "AnaDimitricc",
                                  :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"
                                  :created-at (java.util.Date.)
                                  :updated-at (java.util.Date.)
                                  :favorite-author "George Orwell"
                                  :streak {:total 10 :claimed-today false}
                                  :reading-goals []}
                  "MarijaArsenijevicc" {:id 6
                                        :first-name "Marija"
                                        :last-name "Arsenijevic"
                                        :username "MarijaArsenijevicc"
                                        :password "bcrypt+sha512$4bb7bccc40015d65cd92b3fed76156ba$12$1afe632da0213da578999030808b0c932c0dd361152b0498"
                                        :created-at (java.util.Date.)
                                        :updated-at (java.util.Date.)
                                        :favorite-author "J.K. Rowling"
                                        :streak {:total 10 :claimed-today false}
                                        :reading-goals []}}))


(def books (atom [{:id 1
                   :title "The Hobbit"
                   :popularity 95
                   :pages 310
                   :genres ["Fantasy"]
                   :streak-cost 5
                   :author "J.R.R. Tolkien"}
                  {:id 2
                   :title "Dune"
                   :popularity 90
                   :pages 412
                   :genres ["Fantasy"]
                   :streak-cost 5
                   :author "Frank Herbert"}
                  {:id 3
                   :title "The Great Gatsby"
                   :popularity 85
                   :pages 180
                   :genres ["Fiction"]
                   :streak-cost 3
                   :author "F. Scott Fitzgerald"}
                  {:id 4
                   :title "1984"
                   :popularity 80
                   :pages 328
                   :genres ["Dystopian"]
                   :streak-cost 4
                   :author "George Orwell"}
                  {:id 5
                   :title "War and Peace"
                   :popularity 75
                   :pages 1225
                   :genres ["Historical Fiction"]
                   :streak-cost 6
                   :author "Leo Tolstoy"}
                  {:id 6
                   :title "The Lord of the Rings: The Fellowship of the Ring"
                   :popularity 92
                   :pages 423
                   :genres ["Fantasy"]
                   :streak-cost 5
                   :author "J.R.R. Tolkien"}
                  {:id 7
                   :title "The Lord of the Rings: The Two Towers"
                   :popularity 89
                   :pages 352
                   :genres ["Fantasy"]
                   :streak-cost 5
                   :author "J.R.R. Tolkien"}]))

(def collections (atom [{:id 1
                         :user-id 1
                         :name "Fantasy Favorites"
                         :description "My favorite fantasy books"
                         :public true
                         :created-at (java.util.Date.)
                         :updated-at (java.util.Date.)}
                        {:id 2
                         :user-id 1
                         :name "To Read in 2025"
                         :description "Reading list for next year"
                         :public false
                         :created-at (java.util.Date.)
                         :updated-at (java.util.Date.)}]))

(def subscribers (atom [{:collection-id 1
                         :user-id 2}]))

(def user-book (atom [{:user-id 1
                       :book-id 1
                       :collection-id 1
                       :status "reading"
                       :progress 164
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 1
                       :book-id 3
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 1
                       :book-id 5
                       :collection-id 2
                       :status "reading"
                       :progress 164
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 5
                       :book-id 1
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 5
                       :book-id 3
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 5
                       :book-id 2
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 5
                       :book-id 7
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 5
                       :book-id 6
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 6
                       :book-id 1
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 6
                       :book-id 3
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 6
                       :book-id 5
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}
                      {:user-id 2
                       :book-id 2
                       :collection-id nil
                       :status "want-to-read"
                       :progress nil
                       :added-at (java.util.Date.)
                       :updated-at (java.util.Date.)}]))

(def notes (atom [{:id 1
                   :user-id 1
                   :book-id 1
                   :note "This book is a masterpiece."}]))


(def reviews (atom [{:id 1
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
                     :updated-at (java.util.Date. 1234567890000)}]))

(def notifications (atom [{:id 1
                           :user-id 1
                           :type :new-book
                           :book-id 4
                           :message "A new book published by your favourite author!"}]))