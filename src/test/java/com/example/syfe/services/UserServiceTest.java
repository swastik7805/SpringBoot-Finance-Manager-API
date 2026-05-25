package com.example.syfe.services;

import com.example.syfe.dtos.requests.RegisterRequest;
import com.example.syfe.dtos.responses.AuthResponse;
import com.example.syfe.exceptions.DuplicateResourceException;
import com.example.syfe.models.User;
import com.example.syfe.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("1234567890");

        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("encoded_password")
                .fullName("Test User")
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = userService.registerUser(registerRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateUsername() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }
}
