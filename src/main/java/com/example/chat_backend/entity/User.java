package com.example.chat_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"user\"")  // экранируем имя таблицы, так как user — зарезервированное слово
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // Геттеры и сеттеры вручную
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
