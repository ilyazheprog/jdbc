package org.example;

import org.example.service.MusicService;
import org.example.model.Music;
import org.example.service.BooksVisitorsService;

import java.io.IOException;
import java.lang.StackWalker.Option;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        MusicService service;
        BooksVisitorsService booksVisitorsService;
        try {
            DataSourceManager manager = DataSourceManager.getInstance();
            service = new MusicService(manager.getDataSource());
            booksVisitorsService = new BooksVisitorsService(manager.getDataSource());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        
        // #1
        System.out.println("#1\n-----------------");
        service.getMusicName().orElse(new ArrayList<>()).forEach(System.out::println);

        // #2
        System.out.println("#2\n-----------------");
        service.getMusicNameWithoutMT().orElse(new ArrayList<>()).forEach(System.out::println);

        // #3
        Optional<List<Music>> musicList = service.findAll();
        Integer id = musicList.map(list -> list.size() + 1).orElse(1);

        if (service.addMusic(id, "Govnovoz AI cover")) {
            System.out.println("New Music added successfully");
        } else {
            System.out.println("Failed to add new music");
        }
        

        // #4
        
        System.out.println("#4: Creating tables\n-----------------");
        booksVisitorsService.createTables();

        try {
            // Чтение данных из JSON
            WorkWithJson workWithJson = new WorkWithJson();
            List<Visitor> visitors = workWithJson.GetVisitors();
            Set<Book> uniqueBooks = workWithJson.GetUniqueBooks(visitors);

            for (Book book : uniqueBooks) {
                booksVisitorsService.addBook(book);
            }

            // Добавление пользователей и их книг
            System.out.println("Adding visitors and their books...");
            for (Visitor visitor : visitors) {
                booksVisitorsService.addUserAndBooks(visitor);
            }

            // Получение отсортированного списка книг
            List<Book> sortedBooks = booksVisitorsService.getSortedBooks();
            System.out.println("\nSorted Books:");
            sortedBooks.forEach(System.out::println);

            // Получение книг, опубликованных после 2000 года
            List<Book> booksAfter2000 = booksVisitorsService.getBooksAfter2000();
            System.out.println("\nBooks After 2000:");
            booksAfter2000.forEach(System.out::println);

            List<Book> IlyaBooks = List.of(
                new Book("Clean Code", "Robert C. Martin", 2008, "9780132350884", "Prentice Hall"),
                new Book("Effective Java", "Joshua Bloch", 2017, "9780134685991", "Addison-Wesley")
            );

            List<Book> VovaBooks = List.of(
                new Book("The Pragmatic Programmer", "Andrew Hunt, David Thomas", 1999, "9780201616224", "Addison-Wesley"),
                new Book("Design Patterns", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", 1994, "9780201633610", "Addison-Wesley")
            );
            
            for (Book book : IlyaBooks) {
                booksVisitorsService.addBook(book);
            }
            
            for (Book book : VovaBooks) {
                booksVisitorsService.addBook(book);
            }

            // Добавление информации о себе
            Visitor Ilya = new Visitor("Ilya", "Zhenetskij", "555-999-8888", true, IlyaBooks);
            booksVisitorsService.addUserAndBooks(Ilya);
            
            Visitor Vova = new Visitor("Vova", "Mastihin", "555-999-7777", true, VovaBooks);
            booksVisitorsService.addUserAndBooks(Vova);
            // Получение информации о себе
            System.out.println("\nIlya:");
            System.out.println(booksVisitorsService.getUserInfo("555-999-8888"));

            System.out.println("\nVova:");
            System.out.println(booksVisitorsService.getUserInfo("555-999-7777"));

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        } finally {
            // Удаление таблиц
            System.out.println("\nDropping tables...");
            booksVisitorsService.dropTables();
        }


    }
}