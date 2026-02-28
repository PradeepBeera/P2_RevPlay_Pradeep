package com.rev.app.controller;

import com.rev.app.dto.request.PlaylistRequest;
import com.rev.app.dto.response.ApiResponse;
import com.rev.app.dto.response.PlaylistResponse;
import com.rev.app.service.interfaces.PlaylistService;
import com.rev.app.util.Constants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Constants.PLAYLISTS_PREFIX)
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PlaylistResponse>> createPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlaylistRequest request) {
        PlaylistResponse response = playlistService.createPlaylist(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Playlist created", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PlaylistResponse>>> getMyPlaylists(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlaylistResponse> response = playlistService.getUserPlaylists(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getPlaylistById(@PathVariable Long id) {
        PlaylistResponse response = playlistService.getPlaylistById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<PlaylistResponse>> addSong(
            @PathVariable Long id,
            @RequestParam Long songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse response = playlistService.addSongToPlaylist(id, songId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Song added to playlist", response));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> removeSong(
            @PathVariable Long id,
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse response = playlistService.removeSongFromPlaylist(id, songId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Song removed from playlist", response));
    }

    @PutMapping("/{id}/reorder")
    public ResponseEntity<ApiResponse<PlaylistResponse>> reorderPlaylist(
            @PathVariable Long id,
            @RequestBody List<Long> songIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse response = playlistService.reorderPlaylist(id, songIds, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Playlist reordered", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.deletePlaylist(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Playlist deleted", null));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<PlaylistResponse>> followPlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse response = playlistService.followPlaylist(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Playlist followed", response));
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollowPlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.unfollowPlaylist(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Playlist unfollowed", null));
    }

    @GetMapping("/followed")
    public ResponseEntity<ApiResponse<List<PlaylistResponse>>> getFollowedPlaylists(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlaylistResponse> response = playlistService.getFollowedPlaylists(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
