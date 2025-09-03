package com.pki.pki_backend.dto;

import com.pki.pki_backend.model.Certificate;
import com.pki.pki_backend.model.CertificateType;

import java.time.LocalDateTime;

public class CertificateDetailsDTO {

    private String serialNumber;
    private String subject;
    private String issuer;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean revoked;
    private CertificateType type;

    // Podrazumevani konstruktor (za svaki slučaj, ali ga ne koristimo direktno)
    public CertificateDetailsDTO() {
    }

    // Konstruktor koji prima Certificate objekat i radi mapiranje
    public CertificateDetailsDTO(Certificate cert) {
        this.serialNumber = cert.getSerialNumber();
        this.subject = cert.getSubject();
        this.issuer = cert.getIssuer();
        this.validFrom = cert.getValidFrom();
        this.validTo = cert.getValidTo();
        this.revoked = cert.isRevoked();
        this.type = cert.getType();
    }

    // Getters and Setters
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
    }
}

