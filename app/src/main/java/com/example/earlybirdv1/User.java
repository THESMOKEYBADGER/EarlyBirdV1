package com.example.earlybirdv1;

import java.util.UUID;

public class User {
    private String username;
    private String email;
    private String password;
    private String userId;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userId = generateUserId();
    }

    public User(String username, String email, String password, String userId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserId() {
        return userId;
    }

    private String generateUserId() {
        return UUID.randomUUID().toString();
    }
}
