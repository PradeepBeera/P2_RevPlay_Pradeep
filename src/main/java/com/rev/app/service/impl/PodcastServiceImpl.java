package com.rev.app.service.impl;

import com.rev.app.dto.request.PodcastEpisodeRequest;
import com.rev.app.dto.request.PodcastRequest;
import com.rev.app.dto.response.PodcastEpisodeResponse;
import com.rev.app.dto.response.PodcastResponse;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.Podcast;
import com.rev.app.entity.PodcastEpisode;
import com.rev.app.entity.User;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.ArtistRepository;
import com.rev.app.repository.PodcastEpisodeRepository;
import com.rev.app.repository.PodcastRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.PodcastService;
import com.rev.app.util.FileStorageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PodcastServiceImpl implements PodcastService {

    private final PodcastRepository podcastRepository;
    private final PodcastEpisodeRepository episodeRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;
    private final EntityDtoMapper mapper;

    public PodcastServiceImpl(PodcastRepository podcastRepository,
            PodcastEpisodeRepository episodeRepository,
            ArtistRepository artistRepository,
            UserRepository userRepository,
            FileStorageUtil fileStorageUtil,
            EntityDtoMapper mapper) {
        this.podcastRepository = podcastRepository;
        this.episodeRepository = episodeRepository;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.fileStorageUtil = fileStorageUtil;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PodcastResponse createPodcast(String email, PodcastRequest request, MultipartFile coverImage) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ArtistProfile artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException("Artist profile required", HttpStatus.FORBIDDEN));

        String coverUrl = (coverImage != null && !coverImage.isEmpty())
                ? fileStorageUtil.storeImageFile(coverImage)
                : null;

        Podcast podcast = Podcast.builder()
                .artistProfile(artist)
                .title(request.getTitle())
                .description(request.getDescription())
                .coverImage(coverUrl)
                .build();

        return mapper.toPodcastResponse(podcastRepository.save(podcast));
    }

    @Override
    @Transactional
    public PodcastEpisodeResponse uploadEpisode(String email, PodcastEpisodeRequest request, MultipartFile audioFile) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Podcast podcast = podcastRepository.findById(request.getPodcastId())
                .orElseThrow(() -> new ResourceNotFoundException("Podcast", "id", request.getPodcastId()));

        if (!podcast.getArtistProfile().getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String audioUrl = fileStorageUtil.storeAudioFile(audioFile);

        PodcastEpisode episode = PodcastEpisode.builder()
                .podcast(podcast)
                .title(request.getTitle())
                .description(request.getDescription())
                .audioUrl(audioUrl)
                .releaseDate(
                        request.getReleaseDate() != null ? LocalDate.parse(request.getReleaseDate()) : LocalDate.now())
                .build();

        return mapper.toPodcastEpisodeResponse(episodeRepository.save(episode));
    }

    @Override
    public PodcastResponse getPodcastById(Long id) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast", "id", id));
        return mapper.toPodcastResponse(podcast);
    }

    @Override
    public List<PodcastResponse> getAllPodcasts() {
        return podcastRepository.findByIsDeletedFalse().stream()
                .map(mapper::toPodcastResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PodcastResponse> getArtistPodcasts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ArtistProfile artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist", "userId", user.getId()));

        return podcastRepository.findByArtistProfileIdAndIsDeletedFalse(artist.getId()).stream()
                .map(mapper::toPodcastResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PodcastEpisodeResponse> getPodcastEpisodes(Long podcastId) {
        return episodeRepository.findByPodcastIdOrderByReleaseDateDesc(podcastId).stream()
                .map(mapper::toPodcastEpisodeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePodcast(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast", "id", id));

        if (!podcast.getArtistProfile().getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        podcast.setIsDeleted(true);
        podcastRepository.save(podcast);
    }
}
