package com.rev.app.controller;

import com.rev.app.dto.request.PasswordResetRequest;
import com.rev.app.dto.request.PasswordUpdateRequest;
import com.rev.app.dto.request.RegisterRequest;
import com.rev.app.dto.response.ApiResponse;
import com.rev.app.dto.response.UserResponse;
import com.rev.app.service.interfaces.AuthService;
import com.rev.app.util.Constants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.AUTH_PREFIX)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Registration successful", response));
    }

    @GetMapping("/security-question")
    public ResponseEntity<ApiResponse<String>> getSecurityQuestion(@RequestParam String email) {
        String question = authService.getSecurityQuestion(email);
        return ResponseEntity.ok(ApiResponse.success("Security question retrieved", question));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody PasswordUpdateRequest request) {
        authService.updatePassword(email, request);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }
}
