package com.pki.pki_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CertificateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String csrContent; // CSR u PEM formatu

    @Column(columnDefinition = "TEXT")
    private String privateKey; // Privatni ključ u PEM formatu

    @Column(nullable = false)
    private String subject; // Subjekt iz CSR-a

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "ca_certificate_id")
    private Certificate caCertificate; // CA koji će potpisati

    @OneToOne(mappedBy = "certificateRequest")
    private Certificate issuedCertificate; // Izdati sertifikat

    private String rejectionReason; // Razlog odbacivanja

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, ISSUED
    }

    // Constructors
    public CertificateRequest() {}

    public CertificateRequest(String csrContent, String subject, User requester) {
        this.csrContent = csrContent;
        this.subject = subject;
        this.requester = requester;
        this.requestDate = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    // Getters
    public Long getId() { return id; }
    public String getCsrContent() { return csrContent; }
    public String getPrivateKey() { return privateKey; }
    public String getSubject() { return subject; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public RequestStatus getStatus() { return status; }
    public User getRequester() { return requester; }
    public Certificate getCaCertificate() { return caCertificate; }
    public Certificate getIssuedCertificate() { return issuedCertificate; }
    public String getRejectionReason() { return rejectionReason; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCsrContent(String csrContent) { this.csrContent = csrContent; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public void setRequester(User requester) { this.requester = requester; }
    public void setCaCertificate(Certificate caCertificate) { this.caCertificate = caCertificate; }
    public void setIssuedCertificate(Certificate issuedCertificate) { this.issuedCertificate = issuedCertificate; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
