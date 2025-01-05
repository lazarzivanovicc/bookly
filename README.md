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

## User Stories

User Story 1: User Wants to Generate a List of Books to Read

User Story 2: User Wants to Generate and View Statistics of Book Collection

User Story 3: User Wants to Get Book Recommendations by Genre

User Story 4: User Wants to Get Book Recommendations by Favorite Author

User Story 5: User Wants to Generate Reading Reminders

User Story 6: User Wants to Track Reading Progress

User Story 7: User Wants to Save Personal Notes About a Book

User Story 8: User Wants to View Book Reviews

User Story 9: User Wants to View Sentiment of Book Reviews

User Story 10: User Wants to Receive Notifications About New Books by Favorite Author(s)

User Story 11: User Wants to Subscribe to Another User's Book List

User Story 12: User Wants to Extend Reading Streak by Checking In

User Story 13: User Wants to Spend Streak to Buy a Book

User Story 14: User Wants to Set and Track Personal Reading Goals

User Story 15: User Wants to Receive Notifications for Book Deals

## Models

Based on the **User Stories** the following models were created:

**User**

- ID
- First Name
- Last Name
- Email
- Password
- Streak Count
- Streak Claimed Today
- Role ID
- Created At
- Updated At

**Book**

- ID
- Title
- Authors
- Genres
- Num. Pages
- Price
- Image
- Description
- Publisher
- Publish Date
- Created At
- Updated At

**Review**

- BookID
- UserID
- Text
- Rating Score
- Sentiment
- Created At
- Updated At

**Role**

- ID
- Name
- Created At
- Updated At

**Note**

- ID
- UserID
- BookID
- Note Text
- Created At
- Updated At

**ReadingReminder**

- ID
- UserID
- BookID
- Reminder Time (datetime)
- Note Text
- Created At
- Updated At

**ReadingProgress**

- UserID
- BookID
- Pages Read
- Created At
- Updated At

**Collection**

- ID
- Name
- Owner - UserID
- Created At
- Update At

**CollectionBook**

- CollectionID
- BookID
- Created At
- Update At

**CollectionUser**

- CollectionID
- UserID
- Created At
- Update At

**CollectionType**

- ID
- Name
- Created At
- Updated At

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2024 FIXME
