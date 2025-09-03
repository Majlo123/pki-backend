package com.pki.pki_backend.dto;

import com.pki.pki_backend.model.CertificateType;

import java.util.Date;

// Glavni DTO za zahtev za izdavanje sertifikata
public class IssueCertificateRequestDTO {

    private CertificateType type;
    private SubjectDataDTO subjectData;
    private Date validFrom;
    private Date validTo;
    private String issuerSerialNumber; // Serijski broj sertifikata izdavaoca (null za Root)

    // Getters and Setters
    public CertificateType getType() { return type; }
    public void setType(CertificateType type) { this.type = type; }
    public SubjectDataDTO getSubjectData() { return subjectData; }
    public void setSubjectData(SubjectDataDTO subjectData) { this.subjectData = subjectData; }
    public Date getValidFrom() { return validFrom; }
    public void setValidFrom(Date validFrom) { this.validFrom = validFrom; }
    public Date getValidTo() { return validTo; }
    public void setValidTo(Date validTo) { this.validTo = validTo; }
    public String getIssuerSerialNumber() { return issuerSerialNumber; }
    public void setIssuerSerialNumber(String issuerSerialNumber) { this.issuerSerialNumber = issuerSerialNumber; }
}
