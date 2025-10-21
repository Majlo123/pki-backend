package com.pki.pki_backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SessionCleanupService {

    private final JwtSessionService jwtSessionService;

    public SessionCleanupService(JwtSessionService jwtSessionService) {
        this.jwtSessionService = jwtSessionService;
    }

    /**
     * Runs every hour to cleanup expired JWT sessions
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 milliseconds
    public void cleanupExpiredSessions() {
        jwtSessionService.cleanupAllExpiredSessions();
    }

    /**
     * Runs daily at 2 AM to perform deep cleanup
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyCleanup() {
        jwtSessionService.cleanupAllExpiredSessions();
    }
}
