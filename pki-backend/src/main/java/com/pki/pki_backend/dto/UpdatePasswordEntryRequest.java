package com.pki.pki_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePasswordEntryRequest {

    @NotBlank(message = "Site name is required")
    private String siteName;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Encrypted password is required")
    private String encryptedPassword; // Updated password encrypted with public key

    private String description;

    // Constructors
    public UpdatePasswordEntryRequest() {}

    public UpdatePasswordEntryRequest(String siteName, String username, String encryptedPassword, String description) {
        this.siteName = siteName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.description = description;
    }

    // Getters and Setters
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
