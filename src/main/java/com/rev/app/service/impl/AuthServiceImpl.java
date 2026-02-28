package com.rev.app.service.impl;

import com.rev.app.dto.request.PasswordResetRequest;
import com.rev.app.dto.request.PasswordUpdateRequest;
import com.rev.app.dto.request.RegisterRequest;
import com.rev.app.dto.response.UserResponse;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.ArtistRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository; // Added for Artist profile creation
    private final PasswordEncoder passwordEncoder;
    private final EntityDtoMapper mapper;

    public AuthServiceImpl(UserRepository userRepository,
            ArtistRepository artistRepository,
            PasswordEncoder passwordEncoder,
            EntityDtoMapper mapper) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        logger.info("Registration attempt for: {} with requested role: {}", request.getEmail(), request.getRole());
        // 1. Check if email or username already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already registered", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("Username already taken", HttpStatus.CONFLICT);
        }

        // 2. Determine the user's role
        Role userRole = Role.LISTENER; // Default
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ARTIST")) {
            userRole = Role.ARTIST;
        }

        // 3. Build the User entity
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .displayName(request.getDisplayName() != null && !request.getDisplayName().isEmpty()
                        ? request.getDisplayName()
                        : request.getUsername())
                .securityQuestion(request.getSecurityQuestion())
                .securityAnswer(request.getSecurityAnswer())
                .build();

        // 4. Save the user to the database
        user = userRepository.save(user);
        logger.info("User registered: {} with role: {}", user.getEmail(), user.getRole());

        // 5. If the user is an Artist, create their default ArtistProfile automatically
        if (user.getRole() == Role.ARTIST) {
            ArtistProfile profile = ArtistProfile.builder()
                    .user(user)
                    .artistName(user.getDisplayName())
                    .build();
            artistRepository.save(profile);
            logger.info("Auto-created ArtistProfile for: {}", user.getEmail());
        }

        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public String getSecurityQuestion(String email) {
        return userRepository.findByEmail(email)
                .map(User::getSecurityQuestion)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getSecurityAnswer() == null
                || !user.getSecurityAnswer().equalsIgnoreCase(request.getSecurityAnswer())) {
            throw new CustomException("Invalid security answer", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException("Invalid current password", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password updated successfully for user: {}", user.getEmail());
    }
}
