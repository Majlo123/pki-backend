package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.CertificateDetailsDTO;
import com.pki.pki_backend.model.Certificate;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.service.CertificateService;
import com.pki.pki_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/certificates")
@CrossOrigin(origins = "http://localhost:4200")
public class UserCertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserService userService;

    @GetMapping("/my-certificates")
    public ResponseEntity<List<CertificateDetailsDTO>> getMyCertificates(Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest().build();
            }

            List<CertificateDetailsDTO> certificates = certificateService.getUserCertificates(currentUser);
            return ResponseEntity.ok(certificates);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{certificateId}/details")
    public ResponseEntity<?> getCertificateDetails(@PathVariable Long certificateId, Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Korisnik nije pronađen"));
            }

            Certificate certificate = certificateService.getCertificateById(certificateId);

            // Proveravamo da li je korisnik vlasnik sertifikata
            if (!certificate.getOwner().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nemate dozvolu za pristup ovom sertifikatu"));
            }

            CertificateDetailsDTO details = new CertificateDetailsDTO(certificate);
            return ResponseEntity.ok(details);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{certificateId}/download")
    public ResponseEntity<?> downloadCertificate(@PathVariable Long certificateId, Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Korisnik nije pronađen"));
            }

            Certificate certificate = certificateService.getCertificateById(certificateId);

            // Proveravamo da li je korisnik vlasnik sertifikata
            if (!certificate.getOwner().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nemate dozvolu za preuzimanje ovog sertifikata"));
            }

            // Konvertujemo sertifikat u PEM format
            String pemCertificate = convertToPem(certificate.getEncodedCertificate());
            byte[] certificateBytes = pemCertificate.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"certificate_" + certificate.getSerialNumber() + ".pem\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(certificateBytes));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{certificateId}/revoke")
    public ResponseEntity<?> revokeCertificate(
            @PathVariable Long certificateId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Korisnik nije pronađen"));
            }

            Certificate certificate = certificateService.getCertificateById(certificateId);

            // Proveravamo da li je korisnik vlasnik sertifikata
            if (!certificate.getOwner().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nemate dozvolu za povlačenje ovog sertifikata"));
            }

            if (certificate.isRevoked()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sertifikat je već povučen"));
            }

            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Razlog povlačenja je obavezan"));
            }

            // Validacija razloga prema X.509 standardu
            if (!isValidRevocationReason(reason)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nevaljan razlog povlačenja"));
            }

            certificate.setRevoked(true);
            certificate.setRevocationReason(reason);
            certificate.setRevocationDate(LocalDateTime.now());

            certificateService.saveCertificate(certificate);

            return ResponseEntity.ok(Map.of("message", "Sertifikat je uspešno povučen"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ca-certificates")
    public ResponseEntity<List<CertificateDetailsDTO>> getAvailableCaCertificates() {
        try {
            List<CertificateDetailsDTO> caCertificates = certificateService.getActiveCaCertificates();
            return ResponseEntity.ok(caCertificates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String convertToPem(String base64Certificate) {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");

        // Dodajemo nove redove svakih 64 karaktera
        String cert = base64Certificate.replaceAll("\\s", "");
        for (int i = 0; i < cert.length(); i += 64) {
            pem.append(cert, i, Math.min(i + 64, cert.length())).append("\n");
        }

        pem.append("-----END CERTIFICATE-----\n");
        return pem.toString();
    }

    private boolean isValidRevocationReason(String reason) {
        // X.509 standardni razlozi za povlačenje sertifikata
        String[] validReasons = {
            "unspecified",
            "keyCompromise",
            "cACompromise",
            "affiliationChanged",
            "superseded",
            "cessationOfOperation",
            "certificateHold",
            "removeFromCRL",
            "privilegeWithdrawn",
            "aACompromise"
        };

        for (String validReason : validReasons) {
            if (validReason.equalsIgnoreCase(reason.trim())) {
                return true;
            }
        }
        return false;
    }
}
