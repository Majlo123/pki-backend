package com.pki.pki_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Serijski broj sertifikata (jedinstven)
    @Column(nullable = false, unique = true)
    private String serialNumber;

    // Subject informacije (npr. CN, O, OU...) - može se mapirati kao string ili objekat
    @Column(nullable = false)
    private String subject;

    // Issuer (informacija o izdavaocu sertifikata)
    @Column(nullable = false)
    private String issuer;

    // Datum od kada važi
    private LocalDateTime validFrom;

    // Datum do kada važi
    private LocalDateTime validTo;

    // Da li je sertifikat povučen
    private boolean revoked;

    // Razlog povlačenja po X.509 standardu (ako postoji)
    private String revocationReason;

    // Vreme kada je povučen (ako jeste)
    private LocalDateTime revocationDate;

    // Da li je CA (true za root i intermediate, false za end-entity)
    private boolean ca;

    // Tip sertifikata: ROOT, INTERMEDIATE, END_ENTITY
    @Enumerated(EnumType.STRING)
    private CertificateType type;

    // Sertifikat u PEM formatu za preuzimanje
    @Lob
    private String encodedCertificate;

    // CA keystore lozinka - čuva se enkriptovano (samo za CA certifikate)
    private String encryptedKeystorePassword;

    // JPA veza prema korisniku (vlasniku) sertifikata
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // Ako je neko drugi issuer ovog sertifikata (npr. za intermediate ili EE)
    @ManyToOne
    @JoinColumn(name = "issuer_certificate_id")
    private Certificate issuerCertificate;
}
