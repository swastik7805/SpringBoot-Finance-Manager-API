package com.example.syfe.controllers;

import com.example.syfe.dtos.requests.LoginRequest;
import com.example.syfe.dtos.requests.RegisterRequest;
import com.example.syfe.dtos.responses.AuthResponse;
import com.example.syfe.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controller for managing user authentication and registration.
 * Handles endpoints for user sign-up, login, and session-based logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Registers a new user in the system.
     *
     * @param request the registration details including username, password, full name, and phone number
     * @return the authenticated user's response with HTTP 201 Created status
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and creates an HTTP session.
     *
     * @param request the login credentials (username and password)
     * @param httpRequest the current HTTP request to create a session
     * @return the authenticated user's response with HTTP 200 OK status
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,HttpServletRequest httpRequest) {
        AuthResponse response = userService.loginUser(request);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return ResponseEntity.ok(response);
    }

    /**
     * Logs out the current user by invalidating the HTTP session and clearing the security context.
     *
     * @param request the current HTTP request containing the session
     * @return a success message confirming logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session!=null) session.invalidate();

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}