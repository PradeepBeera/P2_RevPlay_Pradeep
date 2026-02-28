package com.rev.app.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private String coverImage;
    private String artistName;
    private Long artistId;
    private List<SongResponse> songs;
}
