package edu.cit.paradela.checkmateacademy.features.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@test.com");
        testUser.setPassword("password123");
        testUser.setRole("Student");
    }

    @Test
    void registerUser_Success() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = authService.registerUser(testUser);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void authenticate_Success() {
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        Optional<User> result = authService.authenticate("test@test.com", "password123");

        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get().getEmail());
    }

    @Test
    void authenticate_Fail_WrongPassword() {
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        Optional<User> result = authService.authenticate("test@test.com", "wrongpassword");

        assertFalse(result.isPresent());
    }

    @Test
    void emailExists_True() {
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(testUser));

        boolean exists = authService.emailExists("test@test.com");

        assertTrue(exists);
    }
}