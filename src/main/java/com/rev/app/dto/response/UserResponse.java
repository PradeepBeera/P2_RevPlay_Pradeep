package com.rev.app.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String username;
    private String role;
    private String displayName;
    private String bio;
    private String profilePicture;
    private LocalDateTime createdAt;
}
