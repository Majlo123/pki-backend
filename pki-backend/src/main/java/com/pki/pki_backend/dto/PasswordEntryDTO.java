package com.pki.pki_backend.dto;

import com.pki.pki_backend.model.PasswordEntry;
import java.time.LocalDateTime;

public class PasswordEntryDTO {

    private Long id;
    private String siteName;
    private String username;
    private String encryptedPassword; // Will be sent to frontend for decryption
    private String description;
    private Long certificateId;
    private String certificateSubject; // For display purposes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PasswordEntryDTO() {}

    public PasswordEntryDTO(PasswordEntry passwordEntry) {
        this.id = passwordEntry.getId();
        this.siteName = passwordEntry.getSiteName();
        this.username = passwordEntry.getUsername();
        this.encryptedPassword = passwordEntry.getEncryptedPassword();
        this.description = passwordEntry.getDescription();
        this.certificateId = passwordEntry.getCertificate().getId();
        this.certificateSubject = passwordEntry.getCertificate().getSubject();
        this.createdAt = passwordEntry.getCreatedAt();
        this.updatedAt = passwordEntry.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCertificateId() { return certificateId; }
    public void setCertificateId(Long certificateId) { this.certificateId = certificateId; }

    public String getCertificateSubject() { return certificateSubject; }
    public void setCertificateSubject(String certificateSubject) { this.certificateSubject = certificateSubject; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
