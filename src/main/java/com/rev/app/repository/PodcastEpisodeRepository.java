package com.rev.app.repository;

import com.rev.app.entity.PodcastEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PodcastEpisodeRepository extends JpaRepository<PodcastEpisode, Long> {
    List<PodcastEpisode> findByPodcastIdOrderByReleaseDateDesc(Long podcastId);
}
