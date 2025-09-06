package com.pki.pki_backend.controller;

import com.pki.pki_backend.dto.LoginRequest;
import com.pki.pki_backend.dto.RegisterUserRequest;
import com.pki.pki_backend.dto.UserDto;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterUserRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("Registration successful");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        // @AuthenticationPrincipal automatski uzima ulogovanog korisnika iz Security Contexta
        if (user != null) {
            UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getRole().name());
            return ResponseEntity.ok(userDto);
        }
        // Ako niko nije ulogovan, Spring Security će svakako vratiti 401 pre nego što se metoda izvrši
        return ResponseEntity.notFound().build();
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if (userService.login(request)) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
