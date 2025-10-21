package com.pki.pki_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_sessions")
public class JwtSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", unique = true, nullable = false, length = 1000)
    private String token;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    // Default constructor
    public JwtSession() {}

    // Constructor with all fields
    public JwtSession(Long id, String token, String username, LocalDateTime createdAt,
                     LocalDateTime lastAccessedAt, LocalDateTime revokedAt, boolean active,
                     String userAgent, String ipAddress) {
        this.id = id;
        this.token = token;
        this.username = username;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.revokedAt = revokedAt;
        this.active = active;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
