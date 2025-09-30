package com.pki.pki_backend.repository;

import com.pki.pki_backend.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findBySerialNumber(String serialNumber);
    @Query("SELECT c FROM Certificate c WHERE c.issuer LIKE %:organization%")
    List<Certificate> findAllByOrganization(@Param("organization") String organization);
}
