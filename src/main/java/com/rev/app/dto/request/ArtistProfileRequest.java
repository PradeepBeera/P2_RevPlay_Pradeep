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
public class ArtistProfileRequest {

    @NotBlank(message = "Artist name is required")
    @Size(max = 100, message = "Artist name must not exceed 100 characters")
    private String artistName;

    @Size(max = 50, message = "Genre must not exceed 50 characters")
    private String genre;

    @Size(max = 255)
    private String instagramLink;

    @Size(max = 255)
    private String twitterLink;

    @Size(max = 255)
    private String youtubeLink;

    @Size(max = 255)
    private String spotifyLink;

    @Size(max = 255)
    private String websiteLink;
}
