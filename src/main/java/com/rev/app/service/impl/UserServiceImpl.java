package com.rev.app.service.impl;

import com.rev.app.dto.request.UserProfileUpdateRequest;
import com.rev.app.dto.response.UserResponse;
import com.rev.app.entity.User;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.UserService;
import com.rev.app.util.FileStorageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final EntityDtoMapper mapper;
    private final FileStorageUtil fileStorageUtil;

    public UserServiceImpl(UserRepository userRepository,
            EntityDtoMapper mapper,
            FileStorageUtil fileStorageUtil) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.fileStorageUtil = fileStorageUtil;
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = findUserByEmail(email);
        return mapper.toUserResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = findUserByEmail(email);

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        user = userRepository.save(user);
        logger.info("Profile updated for user: {}", email);
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfilePicture(String email, MultipartFile file) {
        User user = findUserByEmail(email);
        String imageUrl = fileStorageUtil.storeImageFile(file);
        user.setProfilePicture(imageUrl);
        user = userRepository.save(user);
        logger.info("Profile picture updated for user: {}", email);
        return mapper.toUserResponse(user);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
