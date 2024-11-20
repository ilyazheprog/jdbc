package org.example;

import lombok.Data;

@Data
public class Book {
    public String name;
    public String author;
    public int publishingYear;
    public String isbn;
    public String publisher;

    public Book(String name, String author, int publishingYear, String isbn, String publisher) {
        this.name = name;
        this.author = author;
        this.publishingYear = publishingYear;
        this.isbn = isbn;
        this.publisher = publisher;
    }
}
