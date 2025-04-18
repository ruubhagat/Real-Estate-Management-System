package com.example.demo.dto;

public class RegisterRequest {
    // Changed from username to name to match User entity and UserController
    private String name;
    private String email;
    private String password;
    private String role; // e.g., ADMIN, CUSTOMER, PROPERTY_OWNER

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // --- Getters and Setters ---

    // Getter and Setter for 'name'
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getters and Setters for other fields
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}