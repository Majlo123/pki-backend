package com.pki.pki_backend.service;

import com.pki.pki_backend.dto.CertificateRequestDTO;
import com.pki.pki_backend.dto.SubmitCsrRequestDTO;
import com.pki.pki_backend.model.Certificate;
import com.pki.pki_backend.model.CertificateRequest;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.repository.CertificateRepository;
import com.pki.pki_backend.repository.CertificateRequestRepository;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CertificateRequestService {

    @Autowired
    private CertificateRequestRepository certificateRequestRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateService certificateService;

    @Transactional
    public CertificateRequestDTO submitCsrRequest(SubmitCsrRequestDTO request, User requester) {
        try {
            // Parsiranje CSR-a
            PKCS10CertificationRequest csr = parseCsr(request.getCsrContent());
            String subject = csr.getSubject().toString();

            // Validacija CA sertifikata
            Certificate caCertificate = certificateRepository.findById(request.getCaCertificateId())
                    .orElseThrow(() -> new RuntimeException("CA sertifikat ne postoji"));

            if (!caCertificate.isCa()) {
                throw new RuntimeException("Odabrani sertifikat nije CA sertifikat");
            }

            // Kreiranje zahteva
            CertificateRequest certificateRequest = new CertificateRequest();
            certificateRequest.setCsrContent(request.getCsrContent());
            certificateRequest.setPrivateKey(request.getPrivateKey());
            certificateRequest.setSubject(subject);
            certificateRequest.setRequester(requester);
            certificateRequest.setCaCertificate(caCertificate);
            certificateRequest.setRequestDate(LocalDateTime.now());
            certificateRequest.setStatus(CertificateRequest.RequestStatus.PENDING);

            certificateRequest = certificateRequestRepository.save(certificateRequest);

            return new CertificateRequestDTO(certificateRequest);

        } catch (Exception e) {
            throw new RuntimeException("Greška pri obradi CSR zahteva: " + e.getMessage(), e);
        }
    }

    public List<CertificateRequestDTO> getUserRequests(User user) {
        List<CertificateRequest> requests = certificateRequestRepository.findByRequesterOrderByRequestDateDesc(user);
        return requests.stream()
                .map(CertificateRequestDTO::new)
                .collect(Collectors.toList());
    }

    public List<CertificateRequestDTO> getAllRequests() {
        List<CertificateRequest> requests = certificateRequestRepository.findAllByOrderByRequestDateDesc();
        return requests.stream()
                .map(CertificateRequestDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveRequest(Long requestId) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Zahtev ne postoji"));

        if (request.getStatus() != CertificateRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Zahtev nije u pending statusu");
        }

        request.setStatus(CertificateRequest.RequestStatus.APPROVED);
        certificateRequestRepository.save(request);

        // Automatsko izdavanje sertifikata nakon odobrenja
        issueCertificateFromRequest(request);
    }

    @Transactional
    public void rejectRequest(Long requestId, String reason) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Zahtev ne postoji"));

        if (request.getStatus() != CertificateRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Zahtev nije u pending statusu");
        }

        request.setStatus(CertificateRequest.RequestStatus.REJECTED);
        request.setRejectionReason(reason);
        certificateRequestRepository.save(request);
    }

    @Transactional
    private void issueCertificateFromRequest(CertificateRequest request) {
        try {
            PKCS10CertificationRequest csr = parseCsr(request.getCsrContent());
            Certificate caCert = request.getCaCertificate();

            // Generiranje serijskog broja
            BigInteger serialNumber = new BigInteger(128, new Random());

            // Kreiranje sertifikata
            Date notBefore = new Date();
            Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000); // 1 godina

            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    new X500Name(caCert.getSubject()),
                    serialNumber,
                    notBefore,
                    notAfter,
                    csr.getSubject(),
                    csr.getSubjectPublicKeyInfo()
            );

            // Učitavanje CA privatnog ključa iz keystore-a
            PrivateKey caPrivateKey = certificateService.getPrivateKeyFromKeystore(caCert.getSerialNumber());

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .build(caPrivateKey);

            X509Certificate x509Certificate = new JcaX509CertificateConverter()
                    .getCertificate(certBuilder.build(signer));

            // Čuvanje sertifikata u bazi
            Certificate certificate = new Certificate();
            certificate.setSerialNumber(serialNumber.toString());
            certificate.setSubject(csr.getSubject().toString());
            certificate.setIssuer(caCert.getSubject());
            certificate.setValidFrom(notBefore.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            certificate.setValidTo(notAfter.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            certificate.setOwner(request.getRequester());
            certificate.setIssuerCertificate(caCert);
            certificate.setCertificateRequest(request);
            certificate.setCa(false);
            certificate.setRevoked(false);
            certificate.setEncodedCertificate(Base64.getEncoder().encodeToString(x509Certificate.getEncoded()));

            certificate = certificateRepository.save(certificate);

            // Ažuriranje zahteva
            request.setStatus(CertificateRequest.RequestStatus.ISSUED);
            request.setIssuedCertificate(certificate);
            certificateRequestRepository.save(request);

        } catch (Exception e) {
            throw new RuntimeException("Greška pri izdavanju sertifikata: " + e.getMessage(), e);
        }
    }

    private PKCS10CertificationRequest parseCsr(String csrPem) throws IOException {
        try (PEMParser pemParser = new PEMParser(new StringReader(csrPem))) {
            Object parsedObject = pemParser.readObject();
            if (parsedObject instanceof PKCS10CertificationRequest) {
                return (PKCS10CertificationRequest) parsedObject;
            } else {
                throw new IllegalArgumentException("Neispravan CSR format");
            }
        }
    }
}
