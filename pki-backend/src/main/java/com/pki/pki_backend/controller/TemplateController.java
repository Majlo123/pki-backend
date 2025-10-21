package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.TemplateDto;
import com.pki.pki_backend.service.TemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/templates")
@PreAuthorize("hasAuthority('ADMIN')") // Osigurava da samo admin može pristupiti
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * Endpoint za kreiranje novog šablona.
     * Prima podatke iz tela POST zahteva.
     */
    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody TemplateDto dto) {
        try {
            return new ResponseEntity<>(templateService.create(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint za dohvatanje liste svih šablona.
     */
    @GetMapping
    public ResponseEntity<List<TemplateDto>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAll());
    }
}

