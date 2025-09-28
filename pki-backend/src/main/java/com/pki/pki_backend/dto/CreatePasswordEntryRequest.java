package com.pki.pki_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePasswordEntryRequest {

    @NotBlank(message = "Site name is required")
    private String siteName;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Encrypted password is required")
    private String encryptedPassword; // Password already encrypted on frontend with public key

    @NotNull(message = "Certificate ID is required")
    private Long certificateId; // ID of certificate used for encryption

    private String description;

    // Constructors
    public CreatePasswordEntryRequest() {}

    public CreatePasswordEntryRequest(String siteName, String username, String encryptedPassword,
                                    Long certificateId, String description) {
        this.siteName = siteName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.certificateId = certificateId;
        this.description = description;
    }

    // Getters and Setters
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public Long getCertificateId() { return certificateId; }
    public void setCertificateId(Long certificateId) { this.certificateId = certificateId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
