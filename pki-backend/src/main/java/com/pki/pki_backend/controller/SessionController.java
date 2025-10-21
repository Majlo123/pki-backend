package com.pki.pki_backend.controller;

import com.pki.pki_backend.model.JwtSession;
import com.pki.pki_backend.service.JwtSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private JwtSessionService jwtSessionService;

    @GetMapping("/active")
    public ResponseEntity<List<JwtSession>> getActiveSessions(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        
        String username = authentication.getName();
        List<JwtSession> activeSessions = jwtSessionService.getActiveSessionsForUser(username);
        return ResponseEntity.ok(activeSessions);
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokeCurrentSession(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        
        // Get current token from request header and revoke it
        String username = authentication.getName();
        // This would need to be implemented in the service
        jwtSessionService.revokeCurrentSession(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke-all")
    public ResponseEntity<?> revokeAllSessions(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        
        String username = authentication.getName();
        jwtSessionService.revokeAllSessionsForUser(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke/{sessionId}")
    public ResponseEntity<?> revokeSpecificSession(@PathVariable Long sessionId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        
        String username = authentication.getName();
        // Verify session belongs to current user and revoke it
        jwtSessionService.revokeSessionForUser(sessionId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanupExpiredSessions() {
        // This endpoint should be admin-only
        jwtSessionService.cleanupAllExpiredSessions();
        return ResponseEntity.ok().build();
    }
}
