package com.rev.app.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PodcastResponse {
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private String artistName;
    private Long artistId;
    private LocalDateTime createdAt;
    private List<PodcastEpisodeResponse> episodes;
}
