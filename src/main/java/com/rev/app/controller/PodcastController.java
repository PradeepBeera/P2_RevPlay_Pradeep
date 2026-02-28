package com.rev.app.controller;

import com.rev.app.dto.request.PodcastEpisodeRequest;
import com.rev.app.dto.request.PodcastRequest;
import com.rev.app.dto.response.ApiResponse;
import com.rev.app.dto.response.PodcastEpisodeResponse;
import com.rev.app.dto.response.PodcastResponse;
import com.rev.app.service.interfaces.PodcastService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {

    private final PodcastService podcastService;

    public PodcastController(PodcastService podcastService) {
        this.podcastService = podcastService;
    }

    @PostMapping
    public ResponseEntity<PodcastResponse> createPodcast(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") PodcastRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ResponseEntity.ok(podcastService.createPodcast(userDetails.getUsername(), request, coverImage));
    }

    @PostMapping("/episodes")
    public ResponseEntity<PodcastEpisodeResponse> uploadEpisode(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") PodcastEpisodeRequest request,
            @RequestPart("audioFile") MultipartFile audioFile) {
        return ResponseEntity.ok(podcastService.uploadEpisode(userDetails.getUsername(), request, audioFile));
    }

    @GetMapping
    public ResponseEntity<List<PodcastResponse>> getAllPodcasts() {
        return ResponseEntity.ok(podcastService.getAllPodcasts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PodcastResponse> getPodcastById(@PathVariable Long id) {
        return ResponseEntity.ok(podcastService.getPodcastById(id));
    }

    @GetMapping("/artist")
    public ResponseEntity<List<PodcastResponse>> getArtistPodcasts(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(podcastService.getArtistPodcasts(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePodcast(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        podcastService.deletePodcast(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Podcast deleted successfully", null));
    }
}
