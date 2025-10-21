package com.pki.pki_backend.config;

import com.pki.pki_backend.model.Role;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ova klasa se izvršava jednom prilikom pokretanja aplikacije.
 * Njena svrha je da popuni bazu sa osnovnim podacima ako oni ne postoje.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Proveravamo da li admin korisnik već postoji u bazi
        if (!userRepository.existsByEmail("admin@pki.com")) {
            User admin = new User();
            admin.setEmail("admin@pki.com");

            // Postavljamo podrazumevanu lozinku i odmah je enkriptujemo
            // Ovu lozinku možete promeniti po želji
            admin.setPassword(passwordEncoder.encode("admin123"));

            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);
            System.out.println(">>> Kreiran je podrazumevani ADMIN nalog (email: admin@pki.com, lozinka: admin123)");
        }
    }
}
