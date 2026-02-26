package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artist_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String artistName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @Column(length = 500)
    private String bannerImage;

    @Column(name = "instagram_link", length = 255)
    private String instagramLink;

    @Column(name = "twitter_link", length = 255)
    private String twitterLink;

    @Column(name = "youtube_link", length = 255)
    private String youtubeLink;

    @Column(name = "spotify_link", length = 255)
    private String spotifyLink;

    @Column(name = "website_link", length = 255)
    private String websiteLink;
}
