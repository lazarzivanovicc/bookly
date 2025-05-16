# Bookly

**Bookly** is an application developed as part of the **Master's course "Tools and Methods of Artificial Intelligence and Software Engineering"** at the **University of Belgrade, Faculty of Organizational Sciences**. Inspired by the intuitive and personalized user experience of Spotify, Bookly shifts its focus to the realm of books, offering platform for book discovery, management, and reading enhancement.

## Let me explain why you might need Bookly

Picture this: you're staring at your bookshelf (or e-reader) wondering what to read next. Do you:

1. Re-read that book you kinda remember finishing but aren’t sure? 📖🤔
2. Scroll endlessly through online reviews, only to end up more confused? 📱🙃
3. Give up and binge-watch South Park instead? 📺😅

In my case I would typicaly start with option 2 followed by thinking about option 1 just to proceed with option 3. If you are at least a little bit like me, that’s where Bookly comes in to save the day (and your reading list).

Bookly serves up personalized book recommendations tailored to your taste. It is literally your sidekick that is always there to help you make a decision, discover and dive into next reading adventure.

Bookly is like that one super-organized and reliable friend with an incredible memory. It keeps track of every book you say you want to read and makes sure you never forget. And trust me, it won’t let you off the hook when it comes to your reading sessions—**seriously, don’t skip your reading sessions!** And yeah, he also keeps track of what you’ve read (so you don’t accidentally re-read that book again).

So why wouldn’t you need friend like Bookly?

## The Bookly Magic - Recommendation Algorithm in Bookly

This section will elaborate more on the recommendation algorithm used by Bookly, explaining the methodology behind how book suggestions are tailored for individual users. The foundational idea behind it is to recommend books to users based on how other users with similar preferences interacted with the book, or explained in a simpler terms, user gets recommendations from users that have similar interest like him, "interest" being the books they have read - this approach is called Collaborative filtering and it is widely used approach in recommender systems.

In order to better understand this methodology we have to dive one step deeper and talk about the real heart and soul of this recommender - k-nearest neighbours algorithm. KNN is the non parametric supervised machine learning algorithm but in the case of Bookly we are not using it in it's most traditional form, as we are not using it for classification or regression but rather as knn similarity search.

Here is a step-by-step breakdown of how the recommendation proccess works:

1. User Profile Retrieval: When a user requests book recommendations, Bookly first retrieves the complete list of books that this user has read.
2. User Base Comparison: Next, Bookly examines all other users on the platform and compiles their respective reading histories — essentially creating a set of book lists, one for each user.
3. Similarity Calculation Using Jaccard Index: To determine which users are most similar, Bookly uses the Jaccard Index, a statistical measure used to compare the similarity between two sets. In this context, it calculates the proportion of shared books between two users relative to the total number of unique books either of them has read. Mathematically Jacard index for two sets A and B is defined as ration between intersection of sets A and B and union of sets A and B. If two sets share all of the elements Jaccard index will be at its maximum (1) and if there are no elements in the intersection of two sets JI will be at its minimum (0). Another metric that is used in KNN sometimes is Jaccard distance derived from Jaccard index - D_j(A, B) = 1 - J(A, B)
4. Finding Nearest Neighbors: Based on these similarity scores, Bookly selects the top k users (where k is a predefined number) who are most similar to the target user. These users are referred to as the "nearest neighbors."
5. Generating Recommendations: Finally, Bookly aggregates books from the reading lists of these nearest neighbors—specifically those books that the target user has not read yet—and presents them as recommendations.

This approach ensures that the recommendations are not only personalized but also grounded in real, observed behavior across a network of similar users.

Of course this approach comes with it's own problems:

1. Cold start - To generate good recommendations for most users on the platform, Bookly (Collaborative filtering algorithm) requires a substantial number of users who have already read and interacted with a wide range of books.
2. Scalability - If Bookly was a real commerical app with a lot of users this approach would be problematic to scale because we would have to compute Jaccard index between user that sent request and all the other users on platform. This could be sovled by precomputing Jaccard index for all of the combinations of users once a day during the period of least activity of our users but solving this problem surpases the scope of this project.

Further improvements to recommendation algorithm could be made by exploring possibility of integrating Content based filttering, where we could leverage neural networks to create embbedings of books offered on Bookly. This would enable recommendation and search based on the similarity between book and would create a more realistic recommender system that is often seen in real production scenarios (hybrid recommender system).

## Bookly helps you make faster and more confident decisions

To enable faster and easier decisioning for users, Bookly, integrates a fine-tuned [RoBERTa-based transformer model](https://huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest) to determine the sentiment (positive, neutral, or negative) of each book review submitted by users - once we obtain sentiment of each comment we can display the numbers and totals (positive review vs negative reviews) based on which users can make faster decisions and feel more confident in their decision without reading throught the reviews.

In this subsection we will expolre the model used for the sentiment analysis RoBERTa - RoBERTa model has the same architecture as BERT and the main difference is actually in pretraining techinques used (dynamic masking, sentence packing, larger batches...). For the curious reader you can find more here [RoBERTa: A Robustly Optimized BERT Pretraining Approach](https://arxiv.org/pdf/1907.11692). But let's make a step back and look why is a BERT base model even a good choice for sentiment analysis task. BERT is bidirectional transformer, actually just the encoder part of the original transformer proposed in [Attention is all you need](https://arxiv.org/pdf/1706.03762), and it was pretrained on unlabled data (text) where the goal of a training was to predict a randomly masked token. Biderectional attention mechanism that BERT uses allowes it to look at the sentence from both sides (left and right)in order to determine the masked token successfully. This kind of attention gives encoder model a more thorough understanding of a sentence and makes it suitable choice for a lot of NLP tasks, one of them being sentiment analysis.

Specific version of RoBERTa used in this project was fine-tuned on Twitter data (124M tweets) and was selected as a popular solution that was hosted on [HuggingFace](https://huggingface.co/), making it accessible through API, which also helped me develop a bit of intuition of how to make a call to different Web APIs using Clojure.

## Compojure, Ring, JWT and atoms

For the development of Bookly I decided to use [Compojure](https://github.com/weavejester/compojure) and [Ring](https://github.com/ring-clojure/ring) as these libraries often represent the backbone of a lot of web applications written in Clojure that I was able to find. Ease of setup of these two and the fact that writting BE API does not require usage of bloated framework made this really enjoyable experience.

For the purpose of exploring the realm of security in Clojure applications I decided to use JSON Web tokens. For that purpose I used [buddy-auth](https://github.com/funcool/buddy-auth) module that provides a middleware that enables authentication for my ring handlers. A step further in improving security in this project could be implementation of refresh tokens for example. Additionally in order to sign JWTs [buddy-sign](https://github.com/funcool/buddy-sign) module was used. Another secuirty meassure that I took when working on Bookly was to store user passwords as hash values and in order to do it library called [buddy-hashers](buddy/buddy-hashers) was used.

For the state management and data persistence, I decided to use atoms, as Bookly is my first Clojure project and I wanted to play around with Clojure’s immutable data structures and the simplicity of using atoms for managing state. Using a database from the start would have added a bit of complexity for testing and for anyone who wants to clone this repo and try it out quickly without setting up a database. That said, I did experiment with connecting to a PostgreSQL database using JDBC (see resources.clj) — it was super simple — and I ran a few queries using raw SQL just to get a feel for working with relational DB and Clojure.

## Testing

Based on the recommendations from our subject professor, library called [Midje](https://github.com/marick/Midje) has been used for unit testing. **TDD** (Test Driven Development) approach was used during the process of
creating this software.

## Closing notes

Overall writting Bookly was pretty nice experience and I am really glad I had this course. In the beggining I felt really uncomfortable while programming in Clojure but as the course progressed I really started to appreciate the elegance and simplicity that comes with it. If I had to explain the experience of programming in Clojure to somebody else it would probabily be good to compare it with building with Lego - Clojure has so many amazing functions that do all the work for you and your job is just to stack them on top of each other. Even though I know that Bookly is miles far from production ready application I wish it to be, this experience made me a more complete programmer so I recommend you clone this repo and start playing with Clojure. I hope you will like it!

## Resources

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

Create Python virtual environment in the root folder

    python -m venv .venv - Windows
    python3 -m venv .venv - Mac/Linux

Activate the virtual environment

    .venv\Scripts\activate - Windows
    source .venv/bin/activate - Mac/Linux

Install the requirements

    pip install -r requirements.txt

To start a web server for the application, run:

    lein ring server

Make sure to create your own .env file based on .env.example

## License

Copyright © 2024 FIXME
