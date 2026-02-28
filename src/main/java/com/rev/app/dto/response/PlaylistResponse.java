package com.rev.app.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistResponse {

    private Long id;
    private String name;
    private String description;
    private String privacy;
    private Long userId;
    private String ownerName;
    private LocalDateTime createdAt;
    private int songCount;
    private Long followerCount;
    private Boolean isFollowed;
    private List<SongResponse> songs;
}
