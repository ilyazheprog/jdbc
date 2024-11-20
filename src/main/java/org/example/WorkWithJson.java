package org.example;

import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WorkWithJson {
    private Gson gson = new Gson();
    private InputStreamReader reader;
    private Type visitorListType = new TypeToken<List<Visitor>>() {}.getType();

    WorkWithJson() {
        // Используем InputStreamReader для загрузки файла из ресурсов
        this.reader = new InputStreamReader(WorkWithJson.class.getClassLoader().getResourceAsStream("books.json"));
        if (this.reader == null) {
            throw new NullPointerException("Файл books.json не найден в ресурсах.");
        }
    }
    List<Visitor> GetVisitors() {
        return gson.fromJson(reader, visitorListType);
    }
    Set<Book> GetUniqueBooks(List<Visitor> visitors) {
        return visitors.stream()
                .flatMap(visitor -> visitor.getFavoriteBooks().stream())
                .collect(Collectors.toSet());
    }
    List<Book> GetSortedBooks(Set<Book> uniqueBooks) {
        return uniqueBooks.stream()
                .sorted(Comparator.comparingInt(Book::getPublishingYear))
                .collect(Collectors.toList());
    }
}
