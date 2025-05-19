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
   [next.jdbc :as jdbc]
   [clojure.java.shell :refer [sh]]
   [bookly.data :refer :all]))

;; Checking DB Connection
;; (jdbc/execute! db ["select * from users"])

(def huggingface-endpoint
  "https://router.huggingface.co/hf-inference/models/cardiffnlp/twitter-roberta-base-sentiment")


(def huggingface-token (env "HF_TOKEN"))


(def secret (env "SECRET_KEY"))

;; ----------------------------------------------------------------------------
;; Registration & Login
;; ----------------------------------------------------------------------------
(defn register
  [request]
  (let [first-name (get-in request [:body "first-name"])
        last-name (get-in request [:body "last-name"])
        username (get-in request [:body "username"])
        password (get-in request [:body "password"])
        favorite-author (get-in request [:body "favorite-author"])]
    (if (contains? @users username)
      {:message (str "User " username " already exists")}
      (do
        (swap! users assoc username {:first-name first-name
                                     :last-name last-name
                                     :username username
                                     :password (hashers/derive password)
                                     :created-at (java.util.Date.)
                                     :updated-at (java.util.Date.)
                                     :favorite-author favorite-author})
        {:message (str "User " username " created successfully")}))))

(defn login
  [request]
  (let [username (get-in request [:body "username"])
        password (get-in request [:body "password"])]
    (if (contains? @users username)
      (if (:valid (hashers/verify password (:password (get @users username))))
        (let [claims {:username username
                      :exp (time/plus (java.util.Date.) (time/seconds 86400))}
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
(defn collection-stats [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user-id (:id (get @users username))
          collection-id (get-in req [:body "collection-id"])
          user-books-ids (map :book-id (filter #(and (= (:user-id %) user-id)
                                                     (= (:collection-id %) collection-id)) @user-book))
          books (filter #(contains? (set user-books-ids) (:id %)) @books)
          total-books (count books)
          total-pages (reduce + (map :pages books))
          all-genres (set (flatten (map :genres books)))]
      (response-ok
       {:user-id user-id
        :books books
        :total-books total-books
        :total-pages total-pages
        :average-pages (if (pos? total-books)
                         (/ total-pages total-books)
                         0)
        :genres all-genres}))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 3. User Story - Get Book Recommendations by Genre
;; ----------------------------------------------------------------------------
(defn recommend-by-genre
  [req]
  (if (authenticated? req)
    (let [genre (get-in req [:body "genre"])
          matching-books (filter #(some #{genre} (:genres %)) @books)
          sorted-books (sort-by :popularity > matching-books)]
      (response-ok
       {:genre genre
        :books (map #(select-keys % [:id :title :author :popularity]) sorted-books)}))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 4. User Story - Get Book Recommendations by Favorite Author
;; ----------------------------------------------------------------------------
(defn recommend-by-author
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          favorite-author (:favorite-author (get @users username))
          matching-books (filter #(= (:author %) favorite-author) @books)
          sorted-books (sort-by :popularity > matching-books)]
      (response-ok
       {:author favorite-author
        :books (map #(select-keys % [:id :title :popularity]) sorted-books)}))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 5. User Story - User Generates Reading Reminder
;; ----------------------------------------------------------------------------
(defn create-reading-reminder
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user-id (:id (get @users username))
          book-id (get-in req [:body "book-id"])
          reminder-time (get-in req [:body "reminder-time"])
          note (get-in req [:body "note"])
          user-reading-book (first (filter #(and (= (:user-id %) user-id)
                                                 (= (:book-id %) book-id)
                                                 (= (:status %) "reading"))
                                           @user-book))
          book (first (filter #(= (:id %) book-id) @books))]
      (if (and user-reading-book book)
        (response-ok
         {:user-id user-id
          :book-id book-id
          :reminder-time reminder-time
          :note note
          :pages-left (- (:pages book) (:progress user-reading-book))})
        (throw (ex-info "Book not found or not currently reading"
                        {:type :book-not-found-or-not-currently-reading}))))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 6. User Story - Track Reading Progress of a User
;; ----------------------------------------------------------------------------
(defn track-reading-progress
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user-id (:id (get @users username))
          book-id (get-in req [:body "book-id"])
          user-reading-book (first (filter #(and (= (:user-id %) user-id)
                                                 (= (:book-id %) book-id)
                                                 (= (:status %) "reading"))
                                           @user-book))
          book (first (filter #(= (:id %) book-id) @books))]
      (if (and user-reading-book book)
        (response-ok
         {:book (:title book)
          :total-pages (:pages book)
          :pages-read (:progress user-reading-book)
          :progress (double (* 100 (/ (:progress user-reading-book) (:pages book))))
          :pages-left (- (:pages book) (:progress user-reading-book))})
        (throw (ex-info "Book not found or not currently reading"
                        {:type :book-not-found-or-not-currently-reading}))))
    (response-unauthorized "Unauthorized")))

(track-reading-progress {:identity {:username "LazarZivanovicc"} :body {"book-id" 1}})

;; ----------------------------------------------------------------------------
;; 7. User Story - Leave Personal Note About a Book
;; ----------------------------------------------------------------------------
(defn leave-personal-note [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user-id (:id (get @users username))
          book-id (get-in req [:body "book-id"])
          note (get-in req [:body "note"])
          note-id (inc (count @notes))]
      (swap! notes conj {:id note-id
                         :user-id user-id
                         :book-id book-id
                         :note note})
      (response-ok {:message "Note added successfully"
                    :note-id note-id
                    :note note}))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 8. User Story - User wants to See Book Reviews
;; ----------------------------------------------------------------------------
(defn get-book-reviews
  [book-id]
  (let [book (first (filter #(= (:id %) book-id) @books))
        book-reviews (filter #(= (:book-id %) book-id) @reviews)]
    (if book
      {:reviews book-reviews}
      (throw (ex-info "Book not found"
                      {:type :book-not-found
                       :book-id book-id})))))

(defn book-reviews
  [req]
  (if (authenticated? req)
    (let [book-id (get-in req [:body "book-id"])
          book-reviews (get-book-reviews book-id)]
      (if book-reviews
        (response-ok
         book-reviews)
        (throw (ex-info "Book not found"
                        {:type :book-not-found
                         :book-id book-id}))))
    (response-unauthorized "Unauthorized")))

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
    (cond
      (= (sentiment :label) "LABEL_0") {:label "negative"}
      (= (sentiment :label) "LABEL_1") {:label "neutral"}
      :else {:label "positive"})))


(defn get-review-sentiment
  [req]
  (if (authenticated? req)
    (let [book-id (get-in req [:body "book-id"])
          reviews (get-book-reviews book-id)
          all-reviews (reviews :reviews)
          reviews-with-sentiment (mapv #(assoc % :sentiment (:label (analyze-sentiment (:review %))))
                                       all-reviews)
          sentiment-counts (frequencies (map :sentiment reviews-with-sentiment))]
      (response-ok
       {:review-sentiment reviews-with-sentiment
        :totals {:positive (get sentiment-counts "positive" 0)
                 :negative (get sentiment-counts "negative" 0)
                 :neutral  (get sentiment-counts "neurtal" 0)}}))
    (response-unauthorized "Unauthorized")))
;; ----------------------------------------------------------------------------
;; 10. User Story - Notify Users About New Book by Their Favorite Author
;; ----------------------------------------------------------------------------
(defn notify-users-about-new-book
  [book-id book-title author]
  (let [users (filter #(= author (:favorite-author %)) (vals @users))
        notifications-added
        (for [user users]
          (let [notif {:id (inc (or (apply max (map :id @notifications)) 0))
                       :user-id (:id user)
                       :type :new-book
                       :book-id book-id
                       :message (str "A new book by your favorite author " author " is available: " book-title)}]
            (swap! notifications conj notif)
            notif))]
    {:notifications notifications-added}))

;; ----------------------------------------------------------------------------
;; 11. User Story - User Subscribes to Someone's Book List
;; ----------------------------------------------------------------------------
(defn subscribe-to-collection
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          collection-id (get-in req [:body "collection-id"])
          user-id (:id (get @users username))
          new-subscription {:collection-id collection-id :user-id user-id}
          existing-subscription (first (filter #(and (= user-id (:user-id %))
                                                     (= collection-id (:collection-id %)))
                                               @subscribers))]
      (if existing-subscription
        (throw (ex-info "User already subscribed to the collection"
                        {:type :user-already-subscribed}))
        (swap! subscribers conj new-subscription))
      (response-ok {:new-subscription new-subscription}))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 12. User Story - User Extends his/her Reading Streak by checking-in for a Reading Session  
;; ----------------------------------------------------------------------------
(defn extend-user-streak
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user (get @users username)
          streak (:streak user)]
      (if (not (:claimed-today streak))
        (do
          (swap! users assoc-in [username :streak :claimed-today] true)
          (swap! users update-in [username :streak :total] inc)
          (response-ok {:message "Streak extended!" :streak (get-in @users [username :streak])}))
        (throw (ex-info "Already claimed today" {:type :streak-already-claimed}))))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 13. User Story - User Spends his Streak to unlock a Book
;; ----------------------------------------------------------------------------
(defn spend-streak-to-unlock-book
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user (get @users username)
          book-id (get-in req [:body "book-id"])
          book (first (filter #(= (:id %) book-id) @books))]
      (if (and user book (>= (get-in user [:streak :total]) (:streak-cost book)))
        (do
          (swap! users update-in [username :streak :total] #(- % (:streak-cost book)))
          (let [user-id (:id (get @users username))
                new-user-book {:user-id user-id
                               :book-id book-id
                               :status "want-to-read"
                               :progress nil
                               :added-at (java.util.Date.)
                               :updated-at (java.util.Date.)}]
            (swap! user-book conj new-user-book)
            (response-ok {:user-id (:id (get @users username))
                          :streak (:streak (get @users username))
                          :message "You have successfully unlocked the book"})))
        (response-server-error "You don't have enough streak to unlock this book")))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 14. User Story - User Sets and Tracks Personal Reading Goals
;; ----------------------------------------------------------------------------
(defn set-reading-goal
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          book-id (get-in req [:body "book-id"])
          target-date (get-in req [:body "target-date"])
          book (first (filter #(= (:id %) book-id) @books))
          new-goal {:book book
                    :target-date target-date
                    :status "in-progress"
                    :created-at (java.util.Date.)}]
      (swap! users update-in [username :reading-goals] #(conj (or % []) new-goal))
      (response-ok {:message "Reading goal set successfully."}))
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; 15. User Story - User Gets Recommendations 
;; ----------------------------------------------------------------------------
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


(defn get-embedding
  [text]
  (let [{:keys [exit out err]} (sh ".venv/Scripts/python.exe" "src/bookly/utils/embeddings.py" text)]
    (if (zero? exit)
      (vec (json/parse-string out))
      (throw (ex-info "Python script failed" {:error err})))))


(defn dot
  [v1 v2]
  (if (not= (count v1) (count v2))
    (throw (ex-info "Vectors must have the same number of elements" {}))
    (reduce + 0 (map * v1 v2))))


(defn euclidian-norm
  [v]
  (Math/sqrt (reduce + (map * v v))))


(defn cosine-similarity
  [v1 v2]
  (let [dot-prod (dot v1 v2)
        norm-v1  (euclidian-norm v1)
        norm-v2  (euclidian-norm v2)]
    (cond
      (zero? norm-v1) (throw (ex-info "First vector has zero norm" {:vector :v1}))
      (zero? norm-v2) (throw (ex-info "Second vector has zero norm" {:vector :v2}))
      :else (Double/parseDouble (format "%.2f" (/ dot-prod (* norm-v1 norm-v2)))))))


(defn find-similar-books
  [book-id]
  (let [book (first (filter #(= (:id %) book-id) @books))]
    (if book
      (let [title (:title book)
            author (:author book)
            genres (:genres book)
            ;; I will embed string containing title, author, and genres but this can be expaneded
            embed-str (str title " " author " " genres)
            book-embedding (get-embedding embed-str)
            other-books (remove #(= (:id %) book-id) @books)
            similarities (map (fn [b]
                                (let [other-title (:title b)
                                      other-author (:author b)
                                      other-genres (:genres b)
                                      other-embed-str (str other-title " " other-author " " other-genres)
                                      other-embedding (get-embedding other-embed-str)
                                      sim (cosine-similarity book-embedding other-embedding)]
                                  (assoc b :similarity sim)))
                              other-books)
            sorted-similarities (sort-by :similarity > similarities)
            top-similar (take 3 sorted-similarities)]
        {:book-id book-id
         :title title
         :most-similar-books top-similar})
      nil)))


(defn recommend-books
  [req]
  (if (authenticated? req)
    (let [user-id (get-in req [:body "user-id"])
          n-nearest-neighbours 5
          user-books (get-user-books user-id)
          user-book-ids (set user-books)
          similar-users (similar-users user-id)
          similar-users-ids (map :user-id similar-users)
          top-similar-users-ids (take n-nearest-neighbours similar-users-ids)
          similar-users-books (mapcat get-user-books top-similar-users-ids)
          recommended-book-ids (remove #(contains? user-book-ids %) similar-users-books)
          collaboration-recommended-books (filter #(contains? (set recommended-book-ids) (:id %)) @books)
          content-based-similar-books
          (if (not= 0 (count user-books))
            ;; I decided to pick one random book from user's books which I will use as a reference when meassuring similarity - this will make the recommender run faster than if I repreated the process for all the books that user has 
            (let [random-book-id (rand-nth (seq user-books))
                  result (find-similar-books random-book-id)
                  all-similar (if result (:most-similar-books result) [])
                  filtered-books (remove #(contains? user-book-ids (:id %)) all-similar)]
              (map #(dissoc % :similarity) filtered-books))
            [])
          all-recommendations (set (concat collaboration-recommended-books content-based-similar-books))]
      {:recommendations all-recommendations})
    (response-unauthorized "Unauthorized")))

;; ----------------------------------------------------------------------------
;; REST
;; ----------------------------------------------------------------------------

(defn create-collection
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user-id (:id (get @users username))
          name (get-in req [:body "name"])
          description (get-in req [:body "description"])
          public (get-in req [:body "public"])
          collection-id (inc (count @collections))
          new-collection {:id collection-id
                          :user-id user-id
                          :name name
                          :description description
                          :public public
                          :created-at (java.util.Date.)
                          :updated-at (java.util.Date.)}]
      (swap! collections conj new-collection)
      (response-ok {:message "Collection created successfully"
                    :collection new-collection}))
    (response-unauthorized "Unauthorized")))


(defn add-book-to-collection
  [req]
  (if (authenticated? req)
    (let [username (get-in req [:identity :username])
          user-id (:id (get @users username))
          book-id (get-in req [:body "book-id"])
          collection-id (get-in req [:body "collection-id"])
          collection (first (filter #(and (= (:id %) collection-id)
                                          (= (:user-id %) user-id))
                                    @collections))
          book-exists-in-collection? (some #(and (= (:book-id %) book-id)
                                                 (= (:collection-id %) collection-id))
                                           @user-book)]
      (if collection
        (if (not book-exists-in-collection?)
          (let [new-user-book {:user-id user-id
                               :book-id book-id
                               :collection-id collection-id
                               :status "want-to-read"
                               :progress nil
                               :added-at (java.util.Date.)
                               :updated-at (java.util.Date.)}]
            (swap! user-book conj new-user-book)
            (response-ok {:message "Book added to collection successfully"
                          :user-book new-user-book}))
          (throw (ex-info "Book already exists in the collection"
                          {:type :book-already-exists
                           :user-id user-id
                           :collection-id collection-id
                           :book-id book-id})))
        (throw (ex-info "Collection not found or user not owner of a collection"
                        {:type :collection-not-found
                         :user-id user-id
                         :collection-id collection-id
                         :book-id book-id}))))
    (response-unauthorized "Unauthorized")))

