package com.pki.pki_backend.repository;

import com.pki.pki_backend.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {
}

