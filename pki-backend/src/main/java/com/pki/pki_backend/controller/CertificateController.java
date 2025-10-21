package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.CertificateDetailsDTO;
import com.pki.pki_backend.dto.IssueCertificateRequestDTO;
import com.pki.pki_backend.dto.RevokeRequestDTO;
import com.pki.pki_backend.model.Certificate;
import com.pki.pki_backend.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;
    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }
    @PostMapping("/issue")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CA_USER')")
    public ResponseEntity<CertificateDetailsDTO> issueCertificate(@RequestBody IssueCertificateRequestDTO request) {
        try {

            Certificate newCert = certificateService.issueCertificate(request);
            return new ResponseEntity<>(new CertificateDetailsDTO(newCert), HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    public ResponseEntity<List<CertificateDetailsDTO>> getAllCertificates() {
        List<CertificateDetailsDTO> certificates = certificateService.getAll();
        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }

    // NOVO: Endpoint za povlačenje sertifikata
    @PostMapping("/{serialNumber}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    public ResponseEntity<?> revokeCertificate(@PathVariable String serialNumber, @RequestBody RevokeRequestDTO request) {
        try {
            certificateService.revokeCertificate(serialNumber, request.getReason());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NOVO: Endpoint za preuzimanje sertifikata
    @GetMapping("/{serialNumber}/download")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CA_USER')")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String serialNumber) {
        try {
            byte[] certificateData = certificateService.getCertificateForDownload(serialNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/x-x509-ca-cert"));
            headers.setContentDispositionFormData("attachment", serialNumber + ".crt");

            return new ResponseEntity<>(certificateData, headers, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
