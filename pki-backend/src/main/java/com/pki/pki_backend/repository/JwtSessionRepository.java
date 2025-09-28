package com.pki.pki_backend.repository;

import com.pki.pki_backend.model.JwtSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JwtSessionRepository extends JpaRepository<JwtSession, Long> {

    Optional<JwtSession> findByToken(String token);

    Optional<JwtSession> findByTokenAndActiveTrue(String token);

    List<JwtSession> findByUsernameAndActiveTrue(String username);

    List<JwtSession> findByUsernameAndLastAccessedAtBefore(String username, LocalDateTime cutoff);

    List<JwtSession> findByActiveTrueAndLastAccessedAtBefore(LocalDateTime cutoff);

    @Query("DELETE FROM JwtSession j WHERE j.active = false AND j.revokedAt < :cutoff")
    void deleteExpiredSessions(@Param("cutoff") LocalDateTime cutoff);

    long countByUsernameAndActiveTrue(String username);
}
