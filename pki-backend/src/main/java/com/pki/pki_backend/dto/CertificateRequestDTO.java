package com.pki.pki_backend.dto;

import com.pki.pki_backend.model.CertificateRequest;
import java.time.LocalDateTime;

public class CertificateRequestDTO {
    private Long id;
    private String subject;
    private LocalDateTime requestDate;
    private CertificateRequest.RequestStatus status;
    private String requesterEmail;
    private String caCertificateSubject;
    private String rejectionReason;
    private Long issuedCertificateId;

    // Constructors
    public CertificateRequestDTO() {}

    public CertificateRequestDTO(CertificateRequest request) {
        this.id = request.getId();
        this.subject = request.getSubject();
        this.requestDate = request.getRequestDate();
        this.status = request.getStatus();
        this.requesterEmail = request.getRequester().getEmail();
        this.caCertificateSubject = request.getCaCertificate() != null ?
            request.getCaCertificate().getSubject() : null;
        this.rejectionReason = request.getRejectionReason();
        this.issuedCertificateId = request.getIssuedCertificate() != null ?
            request.getIssuedCertificate().getId() : null;
    }

    // Getters
    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public CertificateRequest.RequestStatus getStatus() { return status; }
    public String getRequesterEmail() { return requesterEmail; }
    public String getCaCertificateSubject() { return caCertificateSubject; }
    public String getRejectionReason() { return rejectionReason; }
    public Long getIssuedCertificateId() { return issuedCertificateId; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public void setStatus(CertificateRequest.RequestStatus status) { this.status = status; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }
    public void setCaCertificateSubject(String caCertificateSubject) { this.caCertificateSubject = caCertificateSubject; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setIssuedCertificateId(Long issuedCertificateId) { this.issuedCertificateId = issuedCertificateId; }
}
