package com.pki.pki_backend.dto;

public class SubmitCsrRequestDTO {
    private String csrContent;
    private String privateKey;
    private Long caCertificateId;

    // Constructors
    public SubmitCsrRequestDTO() {}

    public SubmitCsrRequestDTO(String csrContent, String privateKey, Long caCertificateId) {
        this.csrContent = csrContent;
        this.privateKey = privateKey;
        this.caCertificateId = caCertificateId;
    }

    // Getters
    public String getCsrContent() { return csrContent; }
    public String getPrivateKey() { return privateKey; }
    public Long getCaCertificateId() { return caCertificateId; }

    // Setters
    public void setCsrContent(String csrContent) { this.csrContent = csrContent; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public void setCaCertificateId(Long caCertificateId) { this.caCertificateId = caCertificateId; }
}
