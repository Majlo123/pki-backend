package com.pki.pki_backend.model;

import jakarta.persistence.*;

@Entity
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String commonNameRegex;
    private String subjectAlternativeNamesRegex; // Za sada se ne koristi, ali je tu za budućnost
    private long timeToLiveDays;

    // Čuvamo ekstenzije kao string, npr. "digitalSignature,keyCertSign"
    private String keyUsage;
    private String extendedKeyUsage;

    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCommonNameRegex() { return commonNameRegex; }
    public void setCommonNameRegex(String commonNameRegex) { this.commonNameRegex = commonNameRegex; }
    public String getSubjectAlternativeNamesRegex() { return subjectAlternativeNamesRegex; }
    public void setSubjectAlternativeNamesRegex(String subjectAlternativeNamesRegex) { this.subjectAlternativeNamesRegex = subjectAlternativeNamesRegex; }
    public long getTimeToLiveDays() { return timeToLiveDays; }
    public void setTimeToLiveDays(long timeToLiveDays) { this.timeToLiveDays = timeToLiveDays; }
    public String getKeyUsage() { return keyUsage; }
    public void setKeyUsage(String keyUsage) { this.keyUsage = keyUsage; }
    public String getExtendedKeyUsage() { return extendedKeyUsage; }
    public void setExtendedKeyUsage(String extendedKeyUsage) { this.extendedKeyUsage = extendedKeyUsage; }
}

