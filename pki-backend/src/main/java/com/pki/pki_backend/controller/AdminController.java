package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.CreateCaUserRequest;
import com.pki.pki_backend.dto.IssueCertificateRequestDTO;
import com.pki.pki_backend.service.CertificateService;
import com.pki.pki_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // VAŽNO: Dodati import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')") // Osigurava da samo admin može pristupiti
public class AdminController {

    private final UserService userService;
    private final CertificateService certificateService;

    public AdminController(UserService userService, CertificateService certificateService) {
        this.userService = userService;
        this.certificateService = certificateService;
    }

    @PostMapping("/ca-user")
    // DODATA JE @RequestBody ANOTACIJA
    public ResponseEntity<String> createCaUser(@RequestBody CreateCaUserRequest request) {
        try {
            userService.createCaUser(request);
            return ResponseEntity.ok("CA user successfully created. Password sent to email.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/certificates/issue")
    public ResponseEntity<String> issueCertificate(@RequestBody IssueCertificateRequestDTO request) {
        try {
            certificateService.issueCertificate(request);
            return ResponseEntity.ok("Certificate issued successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error issuing certificate: " + e.getMessage());
        }
    }
}

