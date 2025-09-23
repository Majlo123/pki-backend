package com.pki.pki_backend.service;

import com.pki.pki_backend.dto.CreateCaUserRequest;
import com.pki.pki_backend.dto.LoginRequest;
import com.pki.pki_backend.dto.RegisterUserRequest;
import com.pki.pki_backend.model.EmailConfirmationToken;
import com.pki.pki_backend.model.Role;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.repository.EmailConfirmationTokenRepository;
import com.pki.pki_backend.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender; // Potrebno za slanje email-a
    private final EmailConfirmationTokenRepository tokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, EmailConfirmationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
    }

    // Javna registracija za obične korisnike
    public void registerUser(RegisterUserRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setOrganization(request.getOrganization());
        user.setRole(Role.END_ENTITY_USER);
        user.setEnabled(false);

        userRepository.save(user);

        // Generisanje tokena
        String token = UUID.randomUUID().toString();
        EmailConfirmationToken confirmationToken = new EmailConfirmationToken();
        confirmationToken.setToken(token);
        confirmationToken.setUser(user);
        confirmationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(confirmationToken);

        // Slanje email-a
        String confirmationUrl = "http://localhost:8080/api/auth/confirm-email?token=" + token;
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Email Confirmation");
        mailMessage.setText("Please confirm your email by clicking the following link: " + confirmationUrl);
        mailSender.send(mailMessage);
    }

    /**
     * NOVA METODA: Kreira CA korisnika na zahtev administratora.
     */
    public User createCaUser(CreateCaUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setOrganization(request.getOrganization());
        user.setRole(Role.CA_USER);
        user.setEnabled(true);

        String generatedPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));

        userRepository.save(user);

        // Slanje email-a sa generisanom lozinkom
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getEmail());
        message.setSubject("Your PKI Account has been created");
        message.setText("An account has been created for you. Your temporary password is: " + generatedPassword);
        mailSender.send(message);

        return user;
    }

    public boolean login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .isPresent();
    }

    // Pomoćna metoda za generisanje sigurne, nasumične lozinke
    private String generateRandomPassword() {
        final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 12) // Dužina lozinke
                .map(i -> random.nextInt(CHARS.length()))
                .mapToObj(randomIndex -> String.valueOf(CHARS.charAt(randomIndex)))
                .collect(Collectors.joining());
    }
}
