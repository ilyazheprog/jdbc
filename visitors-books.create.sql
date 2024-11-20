CREATE TABLE IF NOT EXISTS study.visitors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    subscribed BOOLEAN NOT NULL,
    CONSTRAINT unique_phone UNIQUE (phone)
);

CREATE TABLE IF NOT EXISTS study.books (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publishing_year INT,
    isbn VARCHAR(20),
    publisher VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS study.visitor_favorite_books (
    visitor_id INT NOT NULL,
    book_id INT NOT NULL,
    PRIMARY KEY (visitor_id, book_id),
    FOREIGN KEY (visitor_id) REFERENCES study.visitors(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES study.books(id) ON DELETE CASCADE
);