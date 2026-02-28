package com.rev.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PodcastEpisodeRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String description;

    private String releaseDate;

    private Long podcastId;
}
