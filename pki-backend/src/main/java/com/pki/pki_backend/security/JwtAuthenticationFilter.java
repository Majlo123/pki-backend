package com.pki.pki_backend.security;

import com.pki.pki_backend.service.JwtService;
import com.pki.pki_backend.service.JwtSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtSessionService jwtSessionService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                 UserDetailsService userDetailsService,
                                 JwtSessionService jwtSessionService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.jwtSessionService = jwtSessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestPath = request.getServletPath();

        // Skip JWT authentication for auth endpoints and public endpoints
        if (requestPath.contains("/api/auth/login") ||
            requestPath.contains("/api/auth/register") ||
            requestPath.contains("/api/auth/confirm-email") ||
            requestPath.contains("/swagger-ui") ||
            requestPath.contains("/v3/api-docs")) {

            System.out.println("🔓 Skipping JWT filter for public endpoint: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("🔍 No Bearer token found for: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            String userEmail = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Update last accessed time for the session
                    jwtSessionService.updateLastAccessed(jwt);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("✅ JWT authentication successful for: " + userEmail);
                } else {
                    System.out.println("❌ JWT token invalid or revoked for: " + userEmail);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token is invalid or revoked");
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ JWT token parsing failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token format");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
