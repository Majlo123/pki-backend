package com.pki.pki_backend.dto;

// DTO za zahtev koji administrator šalje za kreiranje novog CA korisnika
public class CreateCaUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String organization;

    // Getters
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getOrganization() { return organization; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setOrganization(String organization) { this.organization = organization; }
}
