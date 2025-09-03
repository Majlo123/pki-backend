package com.pki.pki_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String issuer;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean revoked;
    private String revocationReason;
    private LocalDateTime revocationDate;
    private boolean ca;

    @Enumerated(EnumType.STRING)
    private CertificateType type;

    @Lob
    private String encodedCertificate; // Ovo polje za sada ne koristimo, ali neka ostane

    private String encryptedKeystorePassword;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "issuer_certificate_id")
    private Certificate issuerCertificate;

    // Getters
    public Long getId() { return id; }
    public String getSerialNumber() { return serialNumber; }
    public String getSubject() { return subject; }
    public String getIssuer() { return issuer; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public LocalDateTime getValidTo() { return validTo; }
    public boolean isRevoked() { return revoked; }
    public String getRevocationReason() { return revocationReason; }
    public LocalDateTime getRevocationDate() { return revocationDate; }
    public boolean isCa() { return ca; }
    public CertificateType getType() { return type; }
    public String getEncodedCertificate() { return encodedCertificate; }
    public String getEncryptedKeystorePassword() { return encryptedKeystorePassword; }
    public User getOwner() { return owner; }
    public Certificate getIssuerCertificate() { return issuerCertificate; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }
    public void setRevocationDate(LocalDateTime revocationDate) { this.revocationDate = revocationDate; }
    public void setCa(boolean ca) { this.ca = ca; }
    public void setType(CertificateType type) { this.type = type; }
    public void setEncodedCertificate(String encodedCertificate) { this.encodedCertificate = encodedCertificate; }
    public void setEncryptedKeystorePassword(String encryptedKeystorePassword) { this.encryptedKeystorePassword = encryptedKeystorePassword; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setIssuerCertificate(Certificate issuerCertificate) { this.issuerCertificate = issuerCertificate; }
}
