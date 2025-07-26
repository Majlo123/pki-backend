package com.pki.pki_backend.service;

import com.pki.pki_backend.dto.LoginRequest;
import com.pki.pki_backend.dto.RegisterUserRequest;
import com.pki.pki_backend.model.Role;
import com.pki.pki_backend.model.User;
import com.pki.pki_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(RegisterUserRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // BEZ enkripcije za sada
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setOrganization(request.getOrganization());
        user.setRole(Role.CA_USER); // običan korisnik
        user.setEnabled(true); // za sad svi korisnici aktivni odmah

        userRepository.save(user);
    }

    public boolean login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .isPresent();
    }
}
