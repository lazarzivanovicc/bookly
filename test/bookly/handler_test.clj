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
      (collection-stats) => {:total-books 5, :total-pages 2455, :average-pages 491, :genres #{"Fantasy" "Sci-Fi"}})


(fact "Test recommendations by genre"
      (recommend-by-genre) =not=> nil)


(fact "Test recommendations by user's preferred author"
      (recommend-by-author) => {:author "George Orwell", :books ["1984" "Animal Farm"]})

(fact "Test collaborative recommendations"
      (recommend) =not=> nil
      (recommend) => '({:id 7, :title "The Fellowship of the Ring", :popularity 94}
                       {:id 6, :title "Game of Thrones", :popularity 91}
                       {:id 2, :title "Dune", :popularity 90}
                       {:id 4, :title "1984", :popularity 80}))


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
                                 "password" "fakepass"}}
            req-existing-user {:body {"username" "LazarZivanovicc"
                                      "first-name" "Lazar"
                                      "last-name" "Zivanovic"
                                      "password" "fakepass1"}}]
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
                                                :exp (time/plus (time/now) (time/seconds 86400))}
                                               "secret" {:alg :hs512})}
        (login req-invalid-username) => {:message "Invalid login data please try again"}
        (login req-invalid-password) => {:message "Invalid login data please try again"}))





;; TODO
;; How can I test my endpoints (app (mock/request :get "/api/collection-stats")) returns error 404? Why? It works in Postman and Browser!
;; Possibly organize tests in groups (facts is used as a container for multiple fact statements), single simple test case should be represented with a fact
;; Do tests run immedietly when I start the ring server?