package com.pki.pki_backend.service;

import com.pki.pki_backend.dto.IssueCertificateRequestDTO;
import com.pki.pki_backend.dto.SubjectDataDTO;
import com.pki.pki_backend.model.Certificate;
import com.pki.pki_backend.model.CertificateType;
import com.pki.pki_backend.repository.CertificateRepository;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final PasswordEncoder passwordEncoder;

    // Pomoćna privatna klasa za čuvanje podataka o izdavaocu
    private static class IssuerData {
        private final PrivateKey privateKey;
        private final X509Certificate certificate;

        public IssuerData(PrivateKey privateKey, X509Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }
        public PrivateKey getPrivateKey() { return privateKey; }
        public X509Certificate getCertificate() { return certificate; }
    }

    public CertificateService(CertificateRepository certificateRepository, PasswordEncoder passwordEncoder) {
        this.certificateRepository = certificateRepository;
        this.passwordEncoder = passwordEncoder;
        Security.addProvider(new BouncyCastleProvider());
    }

    public Certificate issueCertificate(IssueCertificateRequestDTO request) throws Exception {
        KeyPair keyPair = generateKeyPair();
        X500Name subjectName = buildX500Name(request.getSubjectData());
        String serialNumber = new BigInteger(128, new SecureRandom()).toString();

        Date validFrom = request.getValidFrom();
        Date validTo = request.getValidTo();

        X509Certificate issuedCertificate;
        Certificate certificateEntity = new Certificate();
        String encryptedPassword = null; // Inicijalno null

        if (request.getType() == CertificateType.ROOT || request.getType() == CertificateType.INTERMEDIATE) {
            IssuerData issuerData;
            X509Certificate[] chain;

            X509v3CertificateBuilder certificateBuilder;

            if (request.getType() == CertificateType.ROOT) {
                certificateBuilder = new JcaX509v3CertificateBuilder(
                        subjectName, new BigInteger(serialNumber), validFrom, validTo, subjectName, keyPair.getPublic());

                ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());
                X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
                issuedCertificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);

                certificateEntity.setIssuer(subjectName.toString());
                certificateEntity.setIssuerCertificate(null);
                chain = new X509Certificate[]{issuedCertificate};
            } else { // INTERMEDIATE
                Certificate issuerEntity = certificateRepository.findBySerialNumber(request.getIssuerSerialNumber())
                        .orElseThrow(() -> new RuntimeException("Issuer certificate not found."));
                validateIssuer(issuerEntity, validTo);
                issuerData = loadIssuerDataFromKeystore(issuerEntity);

                certificateBuilder = new JcaX509v3CertificateBuilder(
                        issuerData.getCertificate(), new BigInteger(serialNumber), validFrom, validTo, subjectName, keyPair.getPublic());

                ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(issuerData.getPrivateKey());
                X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
                issuedCertificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);

                issuedCertificate.verify(issuerData.getCertificate().getPublicKey());

                certificateEntity.setIssuer(issuerData.getCertificate().getSubjectX500Principal().getName());
                certificateEntity.setIssuerCertificate(issuerEntity);
                chain = new X509Certificate[]{issuedCertificate, issuerData.getCertificate()};
            }

            addCaExtensions(certificateBuilder);
            certificateEntity.setCa(true);
            certificateEntity.setType(request.getType());
            encryptedPassword = saveToKeystore(serialNumber, keyPair.getPrivate(), chain);

        } else if (request.getType() == CertificateType.END_ENTITY) {
            Certificate issuerEntity = certificateRepository.findBySerialNumber(request.getIssuerSerialNumber())
                    .orElseThrow(() -> new RuntimeException("Issuer certificate not found."));
            validateIssuer(issuerEntity, validTo);
            IssuerData issuerData = loadIssuerDataFromKeystore(issuerEntity);

            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                    issuerData.getCertificate(), new BigInteger(serialNumber), validFrom, validTo, subjectName, keyPair.getPublic());

            addEndEntityExtensions(certificateBuilder); // Koristimo ekstenzije za End-Entity

            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(issuerData.getPrivateKey());
            X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
            issuedCertificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);

            issuedCertificate.verify(issuerData.getCertificate().getPublicKey());

            certificateEntity.setIssuer(issuerData.getCertificate().getSubjectX500Principal().getName());
            certificateEntity.setCa(false); // Ovo NIJE CA sertifikat
            certificateEntity.setType(CertificateType.END_ENTITY);
            certificateEntity.setIssuerCertificate(issuerEntity);
            // Privatni ključ se NE ČUVA, pa je encryptedPassword ostaje null

        } else {
            throw new IllegalArgumentException("Unsupported certificate type.");
        }

        certificateEntity.setSerialNumber(serialNumber);
        certificateEntity.setSubject(subjectName.toString());
        certificateEntity.setValidFrom(validFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        certificateEntity.setValidTo(validTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        certificateEntity.setRevoked(false);
        certificateEntity.setEncryptedKeystorePassword(encryptedPassword);

        return certificateRepository.save(certificateEntity);
    }

    private void validateIssuer(Certificate issuer, Date newCertValidToDate) {
        LocalDateTime newCertValidTo = newCertValidToDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (!issuer.isCa()) {
            throw new RuntimeException("Issuer is not a CA.");
        }
        if (issuer.isRevoked()) {
            throw new RuntimeException("Issuer certificate is revoked.");
        }
        if (LocalDateTime.now().isAfter(issuer.getValidTo())) {
            throw new RuntimeException("Issuer certificate has expired.");
        }
        if (newCertValidTo.isAfter(issuer.getValidTo())) {
            throw new RuntimeException("New certificate validity cannot exceed issuer's validity.");
        }
    }

    private IssuerData loadIssuerDataFromKeystore(Certificate issuerEntity) throws Exception {
        String keystorePath = "src/main/resources/keystores/" + issuerEntity.getSerialNumber() + ".p12";
        String encryptedPasswordFromDb = issuerEntity.getEncryptedKeystorePassword();
        if (!passwordEncoder.matches("keystore_pass", encryptedPasswordFromDb)) {
            throw new SecurityException("Could not decrypt keystore password.");
        }
        char[] passwordChars = "keystore_pass".toCharArray();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keystorePath), passwordChars);

        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, passwordChars);
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

        return new IssuerData(privateKey, certificate);
    }

    private String saveToKeystore(String serialNumber, PrivateKey privateKey, X509Certificate[] chain) throws Exception {
        String keystorePath = "src/main/resources/keystores/" + serialNumber + ".p12";
        String keystorePassword = "keystore_pass";
        char[] passwordChars = keystorePassword.toCharArray();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        keyStore.setKeyEntry(serialNumber, privateKey, passwordChars, chain);

        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, passwordChars);
        }

        return passwordEncoder.encode(keystorePassword);
    }

    private void addCaExtensions(X509v3CertificateBuilder certificateBuilder) throws Exception {
        certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        certificateBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign));
    }

    // Nova metoda za ekstenzije End-Entity sertifikata
    private void addEndEntityExtensions(X509v3CertificateBuilder certificateBuilder) throws Exception {
        // BasicConstraints(false) znači da ovaj sertifikat ne može da potpisuje druge
        certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        // KeyUsage definiše da se ključ koristi samo za digitalni potpis
        certificateBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
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
}
