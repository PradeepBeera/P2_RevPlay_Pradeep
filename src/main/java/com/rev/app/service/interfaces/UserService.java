package com.rev.app.service.interfaces;

import com.rev.app.dto.request.UserProfileUpdateRequest;
import com.rev.app.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse getCurrentUser(String email);

    UserResponse getUserById(Long id);

    UserResponse updateProfile(String email, UserProfileUpdateRequest request);

    UserResponse updateProfilePicture(String email, MultipartFile file);
}
