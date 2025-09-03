package com.pki.pki_backend.service;

import com.pki.pki_backend.dto.IssueCertificateRequestDTO;
import com.pki.pki_backend.dto.SubjectDataDTO;
import com.pki.pki_backend.model.Certificate;
import com.pki.pki_backend.model.CertificateType;
import com.pki.pki_backend.repository.CertificateRepository;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final PasswordEncoder passwordEncoder;

    // Lokacija za čuvanje keystore fajlova
    private static final String KEYSTORE_DIRECTORY = "src/main/resources/keystores/";

    public CertificateService(CertificateRepository certificateRepository, PasswordEncoder passwordEncoder) {
        this.certificateRepository = certificateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Certificate issueCertificate(IssueCertificateRequestDTO request) throws Exception {
        // Dodajemo Bouncy Castle kao security provider-a
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        if (request.getType() == CertificateType.ROOT) {
            return issueSelfSignedCertificate(request);
        }
        throw new IllegalArgumentException("Unsupported certificate type for now.");
    }

    private Certificate issueSelfSignedCertificate(IssueCertificateRequestDTO request) throws Exception {
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        X500Name subjectAndIssuerName = buildX500Name(request.getSubjectData());
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());

        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
        ContentSigner contentSigner = contentSignerBuilder.build(privateKey);

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subjectAndIssuerName,
                serialNumber,
                request.getValidFrom(),
                request.getValidTo(),
                subjectAndIssuerName,
                publicKey
        );

        // --- DODAVANJE EKSTENZIJA ---
        // BasicConstraints(true) -> Označava da je sertifikat CA
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        // KeyUsage -> Definiše da se ključ koristi za potpisivanje sertifikata i CRL listi
        certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);

        // --- ČUVANJE PRIVATNOG KLJUČA U KEYSTORE ---
        String keystorePassword = generateRandomPassword();
        saveToKeystore(serialNumber.toString(), privateKey, certificate, keystorePassword);

        // Čuvanje sertifikata u bazu
        Certificate newCertificate = new Certificate();
        newCertificate.setSerialNumber(serialNumber.toString());
        newCertificate.setSubject(subjectAndIssuerName.toString());
        newCertificate.setIssuer(subjectAndIssuerName.toString());
        newCertificate.setValidFrom(request.getValidFrom().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        newCertificate.setValidTo(request.getValidTo().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        newCertificate.setRevoked(false);
        newCertificate.setCa(true);
        newCertificate.setType(CertificateType.ROOT);
        // Čuvamo enkriptovanu lozinku za keystore
        newCertificate.setEncryptedKeystorePassword(passwordEncoder.encode(keystorePassword));

        return certificateRepository.save(newCertificate);
    }

    private void saveToKeystore(String alias, PrivateKey privateKey, X509Certificate certificate, String password) throws Exception {
        // Kreiramo direktorijum ako ne postoji
        Files.createDirectories(Paths.get(KEYSTORE_DIRECTORY));
        String keystorePath = KEYSTORE_DIRECTORY + alias + ".p12";

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null); // Inicijalizujemo prazan keystore

        // Postavljamo privatni ključ i lanac sertifikata (za Root, lanac je samo sam sertifikat)
        keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), new java.security.cert.Certificate[]{certificate});

        // Čuvamo keystore u fajl
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, password.toCharArray());
        }
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    private X500Name buildX500Name(SubjectDataDTO data) {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, data.getCommonName());
        builder.addRDN(BCStyle.O, data.getOrganization());
        builder.addRDN(BCStyle.OU, data.getOrganizationalUnit());
        builder.addRDN(BCStyle.C, data.getCountry());
        builder.addRDN(BCStyle.E, data.getEmail());
        return builder.build();
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString();
    }
}

