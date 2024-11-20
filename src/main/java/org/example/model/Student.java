package org.example.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Student {
    private UUID id;
    private String name;
    private String surname;
    private String email;
}
