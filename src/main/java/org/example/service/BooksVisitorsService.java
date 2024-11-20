package org.example.service;

import javax.sql.DataSource;
import org.example.Book;
import org.example.Visitor;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class BooksVisitorsService {
    private final Connection connection;

    public BooksVisitorsService(DataSource dataSource) throws SQLException {
        this.connection = dataSource.getConnection();
    }

    public void createTables() {
        String createVisitorsTableSQL = """
                CREATE TABLE IF NOT EXISTS study.visitors (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    surname VARCHAR(100) NOT NULL,
                    phone VARCHAR(15) UNIQUE,
                    subscribed BOOLEAN NOT NULL
                );
                """;

        String createBooksTableSQL = """
                CREATE TABLE IF NOT EXISTS study.books (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    author VARCHAR(255) NOT NULL,
                    publishing_year INT,
                    isbn VARCHAR(20) UNIQUE,
                    publisher VARCHAR(255)
                );
                """;

        String createVisitorFavoriteBooksTableSQL = """
                CREATE TABLE IF NOT EXISTS study.visitor_favorite_books (
                    visitor_id INT NOT NULL,
                    book_id INT NOT NULL,
                    PRIMARY KEY (visitor_id, book_id),
                    FOREIGN KEY (visitor_id) REFERENCES study.visitors(id) ON DELETE CASCADE,
                    FOREIGN KEY (book_id) REFERENCES study.books(id) ON DELETE CASCADE
                );
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createVisitorsTableSQL);
            statement.execute(createBooksTableSQL);
            statement.execute(createVisitorFavoriteBooksTableSQL);
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public void dropTables() {
        String dropVisitorFavoriteBooksTableSQL = "DROP TABLE IF EXISTS study.visitor_favorite_books;";
        String dropVisitorsTableSQL = "DROP TABLE IF EXISTS study.visitors;";
        String dropBooksTableSQL = "DROP TABLE IF EXISTS study.books;";

        try (Statement statement = connection.createStatement()) {
            statement.execute(dropVisitorFavoriteBooksTableSQL);
            statement.execute(dropVisitorsTableSQL);
            statement.execute(dropBooksTableSQL);
            System.out.println("Tables dropped successfully.");
        } catch (SQLException e) {
            System.err.println("Error dropping tables: " + e.getMessage());
        }
    }

    public List<Book> getSortedBooks() {
        String sql = "SELECT * FROM study.books ORDER BY publishing_year";
        List<Book> books = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                books.add(new Book(
                        resultSet.getString("name"),
                        resultSet.getString("author"),
                        resultSet.getInt("publishing_year"),
                        resultSet.getString("isbn"),
                        resultSet.getString("publisher")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sorted books: " + e.getMessage());
        }

        return books;
    }

    public List<Book> getBooksAfter2000() {
        String sql = "SELECT * FROM study.books WHERE publishing_year > 2000";
        List<Book> books = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                books.add(new Book(
                        resultSet.getString("name"),
                        resultSet.getString("author"),
                        resultSet.getInt("publishing_year"),
                        resultSet.getString("isbn"),
                        resultSet.getString("publisher")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books after 2000: " + e.getMessage());
        }

        return books;
    }

    public void addBooks(List<Book> books) {
        String insertBookSQL = """
            INSERT INTO study.books (name, author, publishing_year, isbn, publisher)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (isbn) DO NOTHING;
        """;

        try (PreparedStatement bookStatement = connection.prepareStatement(insertBookSQL)) {
            for (Book book : books) {
                bookStatement.setString(1, book.getName());
                bookStatement.setString(2, book.getAuthor());
                bookStatement.setInt(3, book.getPublishingYear());
                bookStatement.setString(4, book.getIsbn());
                bookStatement.setString(5, book.getPublisher());
                bookStatement.addBatch(); // Добавляем в батч
            }
            bookStatement.executeBatch(); // Выполняем все запросы
        } catch (SQLException e) {
            System.err.println("Error adding books: " + e.getMessage());
        }
    }


    public void addBook(Book book) {
        String insertBookSQL = """
            INSERT INTO study.books (name, author, publishing_year, isbn, publisher)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (isbn) DO NOTHING;
        """;

        try (PreparedStatement bookStatement = connection.prepareStatement(insertBookSQL)) {
            bookStatement.setString(1, book.getName());
            bookStatement.setString(2, book.getAuthor());
            bookStatement.setInt(3, book.getPublishingYear());
            bookStatement.setString(4, book.getIsbn());
            bookStatement.setString(5, book.getPublisher());
            bookStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding books: " + e.getMessage());
        }
    }

    public void addUserAndBooks(Visitor visitor) {
        String insertVisitorSQL = "INSERT INTO study.visitors (name, surname, phone, subscribed) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING;";
        String insertFavoriteSQL = "INSERT INTO study.visitor_favorite_books (visitor_id, book_id) VALUES (?, ?) ON CONFLICT DO NOTHING;";
        String getVisitorIdSQL = "SELECT id FROM study.visitors WHERE phone = ?";
        String getBookIdSQL = "SELECT id FROM study.books WHERE isbn = ?";

        try {
            int visitorId;
            try (PreparedStatement visitorStatement = connection.prepareStatement(insertVisitorSQL, Statement.RETURN_GENERATED_KEYS)) {
                visitorStatement.setString(1, visitor.getName());
                visitorStatement.setString(2, visitor.getSurname());
                visitorStatement.setString(3, visitor.getPhone());
                visitorStatement.setBoolean(4, visitor.isSubscribed());
                visitorStatement.executeUpdate();

                try (PreparedStatement getVisitorIdStmt = connection.prepareStatement(getVisitorIdSQL)) {
                    getVisitorIdStmt.setString(1, visitor.getPhone());
                    try (ResultSet resultSet = getVisitorIdStmt.executeQuery()) {
                        if (resultSet.next()) {
                            visitorId = resultSet.getInt("id");
                        } else {
                            throw new SQLException("Visitor ID not found after insertion.");
                        }
                    }
                }
            }

            for (Book book : visitor.getFavoriteBooks()) {
                int bookId;
                try (PreparedStatement getBookIdStmt = connection.prepareStatement(getBookIdSQL)) {
                    getBookIdStmt.setString(1, book.getIsbn());
                    try (ResultSet resultSet = getBookIdStmt.executeQuery()) {
                        if (resultSet.next()) {
                            bookId = resultSet.getInt("id");
                        } else {
                            throw new SQLException("Book ID not found for ISBN: " + book.getIsbn());
                        }
                    }
                }

                try (PreparedStatement favoriteStatement = connection.prepareStatement(insertFavoriteSQL)) {
                    favoriteStatement.setInt(1, visitorId);
                    favoriteStatement.setInt(2, bookId);
                    favoriteStatement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding user and books: " + e.getMessage());
        }
    }

    public Visitor getUserInfo(String phone) {
        String getVisitorSQL = "SELECT * FROM study.visitors WHERE phone = ?";
        String getFavoriteBooksSQL = """
                SELECT b.name, b.author, b.publishing_year, b.isbn, b.publisher
                FROM study.books b
                JOIN study.visitor_favorite_books vfb ON b.id = vfb.book_id
                JOIN study.visitors v ON v.id = vfb.visitor_id
                WHERE v.phone = ?
                """;

        Visitor visitor = null;
        try (PreparedStatement visitorStatement = connection.prepareStatement(getVisitorSQL)) {
            visitorStatement.setString(1, phone);
            try (ResultSet visitorResult = visitorStatement.executeQuery()) {
                if (visitorResult.next()) {
                    visitor = new Visitor(
                            visitorResult.getString("name"),
                            visitorResult.getString("surname"),
                            visitorResult.getString("phone"),
                            visitorResult.getBoolean("subscribed")
                    );
                }
            }

            if (visitor != null) {
                try (PreparedStatement favoriteBooksStatement = connection.prepareStatement(getFavoriteBooksSQL)) {
                    favoriteBooksStatement.setString(1, phone);
                    try (ResultSet booksResult = favoriteBooksStatement.executeQuery()) {
                        List<Book> favoriteBooks = new ArrayList<>();
                        while (booksResult.next()) {
                            favoriteBooks.add(new Book(
                                    booksResult.getString("name"),
                                    booksResult.getString("author"),
                                    booksResult.getInt("publishing_year"),
                                    booksResult.getString("isbn"),
                                    booksResult.getString("publisher")
                            ));
                        }
                        visitor.setFavoriteBooks(favoriteBooks);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user info: " + e.getMessage());
        }

        return visitor;
    }
}
