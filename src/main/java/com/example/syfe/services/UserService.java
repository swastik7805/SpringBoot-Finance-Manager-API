package com.example.syfe.services;

import com.example.syfe.dtos.requests.LoginRequest;
import com.example.syfe.dtos.requests.RegisterRequest;
import com.example.syfe.dtos.responses.AuthResponse;
import com.example.syfe.exceptions.DuplicateResourceException;
import com.example.syfe.models.User;
import com.example.syfe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for user management and authentication.
 * Handles user registration, login, and current user retrieval.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user with the provided details.
     * Encrypts the password and ensures username uniqueness.
     *
     * @param request the registration details
     * @return the authentication response containing user info
     * @throws DuplicateResourceException if the username already exists
     */
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .message("User registered successfully")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .build();
    }

    /**
     * Authenticates a user using the AuthenticationManager and sets the SecurityContext.
     * This context is used to establish the HTTP session.
     *
     * @param request the login credentials
     * @return the authentication response containing user info
     */
    public AuthResponse loginUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        return AuthResponse.builder()
                .message("Login successful")
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
    }

    /**
     * Retrieves the currently authenticated User entity from the database.
     * Relies on the SecurityContextHolder to find the authenticated username.
     *
     * @return the current User entity
     * @throws RuntimeException if the authenticated user cannot be found in the database
     */
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("Authenticated user not found in database"));
    }
}