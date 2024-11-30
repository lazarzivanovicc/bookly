(ns bookly.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bookly.core :refer :all]
            [bookly.handler :refer :all]
            [midje.sweet :refer :all]))

;; (deftest test-app
;;   (testing "main route"
;;     (let [response (app (mock/request :get "/"))]
;;       (is (= (:status response) 200))
;;       (is (= (:body response) "Hello World"))))

;;   (testing "not-found route"
;;     (let [response (app (mock/request :get "/invalid"))]
;;       (is (= (:status response) 404)))))

(fact "Test fetching of user reading list"
      (generate-reading-list) =not=> nil)


(fact "Test fetching of collection stats"
      (collection-stats) => {:total-books 5, :total-pages 2455, :average-pages 491, :genres #{"Fantasy" "Sci-Fi"}})


(fact "Test recommendations by genre"
      (recommend-by-genre) =not=> nil)


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


(fact "Register test"
      (let [req {:body {"username" "Jovan"
                        "first-name" "Jovan"
                        "last-name" "Zivanovic"
                        "password" "fakepass"}}]
        (register req) =not=> nil))


(fact "Login test"
      (let [req {:body {"username" "Jovan"}}]
        (login req) =not=> nil))




;; TODO
;; How can I test my endpoints (app (mock/request :get "/api/collection-stats")) returns error 404? Why? It works in Postman and Browser!
;; Possibly organize tests in groups (facts is used as a container for multiple fact statements), single simple test case should be represented with a fact