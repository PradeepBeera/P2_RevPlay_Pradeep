package com.rev.app.service.interfaces;

import com.rev.app.dto.request.PasswordResetRequest;
import com.rev.app.dto.request.PasswordUpdateRequest;
import com.rev.app.dto.request.RegisterRequest;
import com.rev.app.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    String getSecurityQuestion(String email);

    void resetPassword(PasswordResetRequest request);

    void updatePassword(String email, PasswordUpdateRequest request);
}
