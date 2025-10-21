package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.CertificateDetailsDTO;
import com.pki.pki_backend.dto.CreateCaUserRequest;
import com.pki.pki_backend.dto.IssueCertificateRequestDTO;
import com.pki.pki_backend.dto.RevokeRequestDTO;
import com.pki.pki_backend.model.Certificate;
import com.pki.pki_backend.service.CertificateService;
import com.pki.pki_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final CertificateService certificateService;

    public AdminController(UserService userService, CertificateService certificateService) {
        this.userService = userService;
        this.certificateService = certificateService;
    }

    @PostMapping("/ca-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> createCaUser(@RequestBody CreateCaUserRequest request) {
        try {
            userService.createCaUser(request);
            return ResponseEntity.ok("CA user created successfully. Password sent to email.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/certificates/issue")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> issueCertificate(@RequestBody IssueCertificateRequestDTO request) {
        try {
            Certificate newCertificate = certificateService.issueCertificate(request);
            return ResponseEntity.ok("Certificate issued successfully with serial number: " + newCertificate.getSerialNumber());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error issuing certificate: " + e.getMessage());
        }
    }

    @GetMapping("/certificates")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<CertificateDetailsDTO>> getAllCertificates() {
        List<CertificateDetailsDTO> certificates = certificateService.getAll();
        return ResponseEntity.ok(certificates);
    }

    @PostMapping("/certificates/{serialNumber}/revoke")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> revokeCertificate(@PathVariable String serialNumber, @RequestBody RevokeRequestDTO request) {
        try {
            certificateService.revokeCertificate(serialNumber, request.getReason());
            return ResponseEntity.ok("Certificate with serial number " + serialNumber + " has been revoked.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

