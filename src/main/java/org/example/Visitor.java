package org.example;

import lombok.Data;
import java.util.List;

@Data
public class Visitor {
    private String name;
    private String surname;
    private String phone;
    private boolean subscribed;
    private List<Book> favoriteBooks;

    public Visitor(String name, String surname, String phone, boolean subscribed, List<Book> favoriteBooks) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.subscribed = subscribed;
        this.favoriteBooks = favoriteBooks;
    }

    public Visitor(String name, String surname, String phone, boolean subscribed) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.subscribed = subscribed;
    }
}
