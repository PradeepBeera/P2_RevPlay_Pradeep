package com.rev.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PodcastRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String description;
}
