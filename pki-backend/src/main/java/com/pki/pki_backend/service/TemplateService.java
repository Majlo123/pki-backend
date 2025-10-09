package com.pki.pki_backend.service;

import com.pki.pki_backend.dto.TemplateDto;
import com.pki.pki_backend.model.Template;
import com.pki.pki_backend.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    /**
     * Kreira novi šablon na osnovu DTO-a.
     * @param dto Podaci o šablonu sa frontenda.
     * @return Sačuvani Template entitet.
     */
    public Template create(TemplateDto dto) {
        Template template = new Template();
        template.setName(dto.getName());
        template.setCommonNameRegex(dto.getCommonNameRegex());
        template.setSubjectAlternativeNamesRegex(dto.getSubjectAlternativeNamesRegex());
        template.setTimeToLiveDays(dto.getTimeToLiveDays());
        template.setKeyUsage(dto.getKeyUsage());
        template.setExtendedKeyUsage(dto.getExtendedKeyUsage());
        return templateRepository.save(template);
    }

    /**
     * Vraća listu svih šablona.
     * @return Lista DTO-ova sa podacima o svim šablonima.
     */
    public List<TemplateDto> getAll() {
        return templateRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Pomoćna privatna metoda za mapiranje Entiteta u DTO.
     * @param template Entitet iz baze.
     * @return DTO spreman za slanje klijentu.
     */
    private TemplateDto mapToDto(Template template) {
        TemplateDto dto = new TemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setCommonNameRegex(template.getCommonNameRegex());
        dto.setSubjectAlternativeNamesRegex(template.getSubjectAlternativeNamesRegex());
        dto.setTimeToLiveDays(template.getTimeToLiveDays());
        dto.setKeyUsage(template.getKeyUsage());
        dto.setExtendedKeyUsage(template.getExtendedKeyUsage());
        return dto;
    }
}

