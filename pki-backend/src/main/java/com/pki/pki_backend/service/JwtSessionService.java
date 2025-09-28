package com.pki.pki_backend.service;

import com.pki.pki_backend.model.JwtSession;
import com.pki.pki_backend.repository.JwtSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class JwtSessionService {

    private final JwtSessionRepository jwtSessionRepository;

    public JwtSessionService(JwtSessionRepository jwtSessionRepository) {
        this.jwtSessionRepository = jwtSessionRepository;
    }

    public void addActiveSession(String token, String username) {
        // Remove expired sessions for this user
        cleanupExpiredSessions(username);

        JwtSession session = new JwtSession();
        session.setToken(token);
        session.setUsername(username);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastAccessedAt(LocalDateTime.now());
        session.setActive(true);

        jwtSessionRepository.save(session);
        System.out.println("💾 Added JWT session for user: " + username);
        System.out.println("💾 Token prefix: " + token.substring(0, Math.min(20, token.length())) + "...");
    }

    public boolean isSessionActive(String token) {
        boolean active = jwtSessionRepository.findByTokenAndActiveTrue(token).isPresent();
        System.out.println("🔍 Checking session for token: " + token.substring(0, Math.min(20, token.length())) + "...");
        System.out.println("🔍 Session active in DB: " + active);
        return active;
    }

    public void revokeSession(String token) {
        jwtSessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
            jwtSessionRepository.save(session);
        });
    }

    public void revokeAllUserSessions(String username) {
        List<JwtSession> userSessions = jwtSessionRepository.findByUsernameAndActiveTrue(username);
        LocalDateTime now = LocalDateTime.now();

        userSessions.forEach(session -> {
            session.setActive(false);
            session.setRevokedAt(now);
        });

        jwtSessionRepository.saveAll(userSessions);
    }

    public void updateLastAccessed(String token) {
        jwtSessionRepository.findByTokenAndActiveTrue(token).ifPresent(session -> {
            session.setLastAccessedAt(LocalDateTime.now());
            jwtSessionRepository.save(session);
        });
    }

    public List<JwtSession> getActiveSessions(String username) {
        return jwtSessionRepository.findByUsernameAndActiveTrue(username);
    }

    // New method for the controller
    public List<JwtSession> getActiveSessionsForUser(String username) {
        return getActiveSessions(username);
    }

    // New method for revoking current session (needs current token to be passed)
    public void revokeCurrentSession(String username) {
        // This is a simplified version - in real implementation you'd need the actual token
        // For now, we'll revoke the most recent session
        List<JwtSession> activeSessions = jwtSessionRepository.findByUsernameAndActiveTrue(username);
        if (!activeSessions.isEmpty()) {
            JwtSession mostRecent = activeSessions.get(activeSessions.size() - 1);
            revokeSession(mostRecent.getToken());
        }
    }

    // New method for revoking all sessions for user
    public void revokeAllSessionsForUser(String username) {
        revokeAllUserSessions(username);
    }

    // New method for revoking specific session by ID
    public void revokeSessionForUser(Long sessionId, String username) {
        jwtSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUsername().equals(username) && session.isActive()) {
                revokeSession(session.getToken());
            }
        });
    }

    public void cleanupExpiredSessions(String username) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24); // Clean sessions older than 24h
        List<JwtSession> expiredSessions = jwtSessionRepository.findByUsernameAndLastAccessedAtBefore(username, cutoff);

        expiredSessions.forEach(session -> {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
        });

        if (!expiredSessions.isEmpty()) {
            jwtSessionRepository.saveAll(expiredSessions);
        }
    }

    public void cleanupAllExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<JwtSession> expiredSessions = jwtSessionRepository.findByActiveTrueAndLastAccessedAtBefore(cutoff);

        expiredSessions.forEach(session -> {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
        });

        if (!expiredSessions.isEmpty()) {
            jwtSessionRepository.saveAll(expiredSessions);
        }
    }
}
