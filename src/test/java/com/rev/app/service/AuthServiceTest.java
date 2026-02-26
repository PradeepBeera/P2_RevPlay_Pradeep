package com.rev.app.service;

import com.rev.app.dto.request.RegisterRequest;
import com.rev.app.dto.response.UserResponse;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import com.rev.app.exception.CustomException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.ArtistRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.impl.AuthServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityDtoMapper mapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @Before
    public void setUp() {
        registerRequest = new RegisterRequest("test@email.com", "testuser", "password123", "Test User",
                "What was the name of your first pet?", "Buddy", "LISTENER");

        savedUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .username("testuser")
                .password("$2a$12$encoded")
                .role(Role.LISTENER)
                .displayName("Test User")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    public void register_WithValidData_ReturnsUserResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        UserResponse mockResponse = UserResponse.builder().id(1L).email("test@email.com").build();
        when(mapper.toUserResponse(any(User.class))).thenReturn(mockResponse);

        UserResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@email.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test(expected = CustomException.class)
    public void register_WithDuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);
        authService.register(registerRequest);
    }

    @Test(expected = CustomException.class)
    public void register_WithDuplicateUsername_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        authService.register(registerRequest);
    }

    @Test
    public void register_AsArtist_CreatesArtistProfile() {
        // Set up request for Artist role
        registerRequest.setRole("ARTIST");
        savedUser.setRole(Role.ARTIST);

        // Mock dependencies
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Mock mapper response
        UserResponse mockResponse = UserResponse.builder().id(1L).email("artist@test.com").role("ARTIST").build();
        when(mapper.toUserResponse(any(User.class))).thenReturn(mockResponse);

        // Execute
        UserResponse response = authService.register(registerRequest);

        // Assertions
        assertNotNull(response);
        assertEquals("ARTIST", response.getRole());
        // Verify that artistRepository.save was called, confirming auto-creation
        verify(artistRepository, times(1)).save(any(ArtistProfile.class));
    }
}
