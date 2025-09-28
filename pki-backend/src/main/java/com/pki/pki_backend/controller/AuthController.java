package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.RegisterUserRequest;
import com.pki.pki_backend.dto.LoginRequest;
import com.pki.pki_backend.model.EmailConfirmationToken;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.repository.EmailConfirmationTokenRepository;
import com.pki.pki_backend.repository.UserRepository;
import com.pki.pki_backend.service.UserService;
import com.pki.pki_backend.service.CaptchaService;
import com.pki.pki_backend.service.JwtService;
import com.pki.pki_backend.service.JwtSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

@RestController
public class AuthController {
    private final EmailConfirmationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final CaptchaService captchaService;
    private final JwtService jwtService;
    private final JwtSessionService jwtSessionService;
    private final AuthenticationManager authenticationManager;

    public AuthController(EmailConfirmationTokenRepository tokenRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         UserService userService,
                         CaptchaService captchaService,
                         JwtService jwtService,
                         JwtSessionService jwtSessionService,
                         AuthenticationManager authenticationManager) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.captchaService = captchaService;
        this.jwtService = jwtService;
        this.jwtSessionService = jwtSessionService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok().body("User registered successfully. Please check your email to confirm your account.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/auth/confirm-email")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        Optional<EmailConfirmationToken> confirmationTokenOpt = tokenRepository.findByToken(token);
        if (confirmationTokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid confirmation token.");
        }
        EmailConfirmationToken confirmationToken = confirmationTokenOpt.get();
        if (confirmationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Confirmation token has expired.");
        }
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(confirmationToken);
        return ResponseEntity.ok("Email confirmed successfully. You can now log in.");
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isEnabled()) {
                return ResponseEntity.badRequest().body("Account not activated. Please check your email.");
            }

            // Generate JWT token (JwtService will automatically add session to database)
            String jwtToken = jwtService.generateToken(user);

            // Return token and user info
            return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "user", new UserDto(user.getEmail(), user.getRole().name(), user.isEnabled())
            ));

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid email or password: " + e.getMessage());
        }
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<?> getCurrentUser(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken) {
        
        // Privremeno isključujemo captcha validaciju za development
        // TODO: Omogućiti captcha validaciju u produkciji
        /*
        if (captchaToken != null && !captchaService.verifyCaptcha(captchaToken)) {
            return ResponseEntity.status(400).body("Invalid captcha");
        }
        */

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(java.util.Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            return ResponseEntity.status(401).body("Invalid credentials format");
        }
        String email = values[0];
        String password = values[1];
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
        // Vraćamo DTO sa email, role, enabled
        return ResponseEntity.ok(new UserDto(user.getEmail(), user.getRole().name(), user.isEnabled()));
    }

    public static class UserDto {
        public String email;
        public String role;
        public boolean enabled;
        public UserDto(String email, String role, boolean enabled) {
            this.email = email;
            this.role = role;
            this.enabled = enabled;
        }
    }
}
