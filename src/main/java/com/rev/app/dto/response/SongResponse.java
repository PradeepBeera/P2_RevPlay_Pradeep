package com.rev.app.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongResponse {

    private Long id;
    private String title;
    private String genre;
    private Integer duration;
    private LocalDate releaseDate;
    private String audioUrl;
    private String coverImage;
    private String visibility;
    private String status;
    private Long fileSize;
    private Long playCount;
    private String artistName;
    private Long artistId;
    private String albumName;
    private Long albumId;
}
