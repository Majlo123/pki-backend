package com.pki.pki_backend.dto;

// DTO koji služi za prenos osnovnih informacija o korisniku na frontend
public class UserDto {
    private Long id;
    private String email;
    private String role;

    public UserDto(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    // Getteri
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
