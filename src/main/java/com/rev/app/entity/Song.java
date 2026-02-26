package com.rev.app.entity;

import com.rev.app.entity.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    private Integer duration;

    private LocalDate releaseDate;

    @Column(nullable = false, length = 500)
    private String audioUrl;

    @Column(length = 500)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    @Column(length = 20)
    private String status;

    private Long fileSize;

    @Column(nullable = false)
    @Builder.Default
    private Long playCount = 0L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistProfile artistProfile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "album_id")
    private Album album;
}
