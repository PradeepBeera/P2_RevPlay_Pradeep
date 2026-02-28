package com.rev.app.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PodcastEpisodeResponse {
    private Long id;
    private Long podcastId;
    private String title;
    private String description;
    private String audioUrl;
    private Integer duration;
    private LocalDate releaseDate;
    private LocalDateTime createdAt;
}
