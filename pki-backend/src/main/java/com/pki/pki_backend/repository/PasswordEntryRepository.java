package com.pki.pki_backend.repository;

import com.pki.pki_backend.model.PasswordEntry;
import com.pki.pki_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {

    /**
     * Find all password entries owned by a specific user
     */
    List<PasswordEntry> findByOwnerOrderByCreatedAtDesc(User owner);

    /**
     * Find a specific password entry by ID and owner (for security)
     */
    Optional<PasswordEntry> findByIdAndOwner(Long id, User owner);

    /**
     * Find password entries by site name for a specific user
     */
    List<PasswordEntry> findByOwnerAndSiteNameContainingIgnoreCaseOrderByCreatedAtDesc(User owner, String siteName);

    /**
     * Check if a password entry exists for a specific user, site, and username combination
     */
    boolean existsByOwnerAndSiteNameAndUsername(User owner, String siteName, String username);

    /**
     * Count total password entries for a user
     */
    long countByOwner(User owner);

    /**
     * Find password entries by certificate ID (useful when a certificate is being revoked)
     */
    @Query("SELECT pe FROM PasswordEntry pe WHERE pe.certificate.id = :certificateId")
    List<PasswordEntry> findByCertificateId(@Param("certificateId") Long certificateId);

    /**
     * Delete all password entries for a specific user
     */
    void deleteByOwner(User owner);
}
