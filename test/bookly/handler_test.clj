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

;; TODO
;; How can I test my endpoints (app (mock/request :get "/api/collection-stats")) returns error 404? Why? It works in Postman and Browser!
;; Possibly organize tests in groups (facts is used as a container for multiple fact statements), single simple test case should be represented with a fact