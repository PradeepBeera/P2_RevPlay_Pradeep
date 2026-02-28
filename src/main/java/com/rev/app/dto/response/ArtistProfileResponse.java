package com.rev.app.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistProfileResponse {

    private Long id;
    private Long userId;
    private String artistName;
    private String genre;
    private String bannerImage;
    private String instagramLink;
    private String twitterLink;
    private String youtubeLink;
    private String spotifyLink;
    private String websiteLink;
}
