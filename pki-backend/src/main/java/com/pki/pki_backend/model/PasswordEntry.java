package com.pki.pki_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_entries")
public class PasswordEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String siteName;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedPassword; // Password encrypted with user's public key

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate; // Certificate used for encryption (contains public key)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String description; // Optional description

    // Constructors
    public PasswordEntry() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PasswordEntry(String siteName, String username, String encryptedPassword,
                        User owner, Certificate certificate, String description) {
        this();
        this.siteName = siteName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.owner = owner;
        this.certificate = certificate;
        this.description = description;
    }

    // Getters
    public Long getId() { return id; }
    public String getSiteName() { return siteName; }
    public String getUsername() { return username; }
    public String getEncryptedPassword() { return encryptedPassword; }
    public User getOwner() { return owner; }
    public Certificate getCertificate() { return certificate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getDescription() { return description; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public void setUsername(String username) { this.username = username; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setCertificate(Certificate certificate) { this.certificate = certificate; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setDescription(String description) { this.description = description; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
