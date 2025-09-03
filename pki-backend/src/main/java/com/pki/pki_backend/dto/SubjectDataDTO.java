package com.pki.pki_backend.dto;

// DTO za prenos podataka o vlasniku (subject) sertifikata
public class SubjectDataDTO {
    private String commonName;
    private String organization;
    private String organizationalUnit;
    private String country;
    private String email;

    // Getters and Setters
    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getOrganizationalUnit() { return organizationalUnit; }
    public void setOrganizationalUnit(String organizationalUnit) { this.organizationalUnit = organizationalUnit; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
