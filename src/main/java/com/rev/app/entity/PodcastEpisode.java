package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "podcast_episodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PodcastEpisode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "podcast_id", nullable = false)
    private Podcast podcast;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false, length = 500)
    private String audioUrl;

    private Integer duration;

    private LocalDate releaseDate;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
