package com.rev.app.service.interfaces;

import com.rev.app.dto.request.PodcastEpisodeRequest;
import com.rev.app.dto.request.PodcastRequest;
import com.rev.app.dto.response.PodcastEpisodeResponse;
import com.rev.app.dto.response.PodcastResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PodcastService {
    PodcastResponse createPodcast(String email, PodcastRequest request, MultipartFile coverImage);

    PodcastEpisodeResponse uploadEpisode(String email, PodcastEpisodeRequest request, MultipartFile audioFile);

    PodcastResponse getPodcastById(Long id);

    List<PodcastResponse> getAllPodcasts();

    List<PodcastResponse> getArtistPodcasts(String email);

    List<PodcastEpisodeResponse> getPodcastEpisodes(Long podcastId);

    void deletePodcast(Long id, String email);
}
