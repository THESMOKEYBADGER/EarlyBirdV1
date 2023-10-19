package com.example.earlybirdv1;

import java.util.UUID;

public class User {
    private String username;
    private String email;
    private String password;
    private String userId;

    public User(String username, String email, String password, String userId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userId = generateUserId();
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

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String generateUserId() {
        return UUID.randomUUID().toString();
    }
}
