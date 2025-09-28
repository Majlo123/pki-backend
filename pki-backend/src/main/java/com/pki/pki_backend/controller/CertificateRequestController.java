package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.CertificateRequestDTO;
import com.pki.pki_backend.dto.SubmitCsrRequestDTO;
import com.pki.pki_backend.model.Role;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.service.CertificateRequestService;
import com.pki.pki_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificate-requests")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateRequestController {

    @Autowired
    private CertificateRequestService certificateRequestService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitCsrRequest(
            @RequestParam("csrFile") MultipartFile csrFile,
            @RequestParam(value = "privateKeyFile", required = false) MultipartFile privateKeyFile,
            @RequestParam("caCertificateId") Long caCertificateId,
            Authentication authentication) {

        try {
            if (csrFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "CSR fajl je obavezan"));
            }

            String csrContent = new String(csrFile.getBytes(), StandardCharsets.UTF_8);
            String privateKeyContent = null;

            if (privateKeyFile != null && !privateKeyFile.isEmpty()) {
                privateKeyContent = new String(privateKeyFile.getBytes(), StandardCharsets.UTF_8);
            }

            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Korisnik nije pronađen"));
            }

            SubmitCsrRequestDTO requestDto = new SubmitCsrRequestDTO(csrContent, privateKeyContent, caCertificateId);
            CertificateRequestDTO result = certificateRequestService.submitCsrRequest(requestDto, currentUser);

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Greška pri čitanju fajla: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Greška pri obradi zahteva: " + e.getMessage()));
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<CertificateRequestDTO>> getUserRequests(Authentication authentication) {
        System.out.println("=== getUserRequests called ===");
        try {
            if (authentication == null) {
                System.out.println("Authentication is null");
                return ResponseEntity.badRequest().body(List.of());
            }
            
            String email = authentication.getName();
            System.out.println("Authenticated user email: " + email);
            
            User currentUser = userService.findByEmail(email);
            if (currentUser == null) {
                System.out.println("User not found for email: " + email);
                return ResponseEntity.badRequest().body(List.of());
            }
            
            System.out.println("Found user: " + currentUser.getEmail() + ", role: " + currentUser.getRole());

            List<CertificateRequestDTO> requests = certificateRequestService.getUserRequests(currentUser);
            System.out.println("Found " + requests.size() + " requests for user");
            
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            System.err.println("Error in getUserRequests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<CertificateRequestDTO>> getAllRequests(Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null || !currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<CertificateRequestDTO> requests = certificateRequestService.getAllRequests();
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId, Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null || !currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nemate dozvolu za ovu akciju"));
            }

            certificateRequestService.approveRequest(requestId);
            return ResponseEntity.ok(Map.of("message", "Zahtev je odobren i sertifikat je izdat"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            User currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null || !currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nemate dozvolu za ovu akciju"));
            }

            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Razlog odbacivanja je obavezan"));
            }

            certificateRequestService.rejectRequest(requestId, reason);
            return ResponseEntity.ok(Map.of("message", "Zahtev je odbačen"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
