package com.pki.pki_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}")
    private String secretKey;

    @Value("${app.jwt.expiration:3600000}") // 1 hour default
    private Long jwtExpiration;

    private final JwtSessionService jwtSessionService;

    public JwtService(JwtSessionService jwtSessionService) {
        this.jwtSessionService = jwtSessionService;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        String token = buildToken(extraClaims, userDetails, jwtExpiration);
        // Store token in session store
        jwtSessionService.addActiveSession(token, userDetails.getUsername());
        return token;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            System.out.println("🔍 Validating token for user: " + username);
            System.out.println("🔍 Expected user: " + userDetails.getUsername());
            System.out.println("🔍 Username matches: " + username.equals(userDetails.getUsername()));
            System.out.println("🔍 Token expired: " + isTokenExpired(token));
            System.out.println("🔍 Session active: " + jwtSessionService.isSessionActive(token));

            return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && jwtSessionService.isSessionActive(token);
        } catch (Exception e) {
            System.out.println("❌ Token validation error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public void revokeToken(String token) {
        jwtSessionService.revokeSession(token);
    }

    public String getJwtIdFromToken(String token) {
        return extractClaim(token, claims -> claims.getId());
    }
}
