package com.rev.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SongUploadRequest {

    @NotBlank(message = "Song title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 50, message = "Genre must not exceed 50 characters")
    private String genre;

    private Integer duration;

    private String releaseDate;

    private String visibility;

    private Long albumId;
}
