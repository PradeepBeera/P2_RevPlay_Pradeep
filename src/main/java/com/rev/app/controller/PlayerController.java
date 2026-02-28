package com.rev.app.controller;

import com.rev.app.dto.response.ApiResponse;
import com.rev.app.dto.response.SongResponse;
import com.rev.app.service.interfaces.FavoriteService;
import com.rev.app.service.interfaces.HistoryService;
import com.rev.app.service.interfaces.SongService;
import com.rev.app.util.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(Constants.PLAYER_PREFIX)
public class PlayerController {

    private final FavoriteService favoriteService;
    private final HistoryService historyService;
    private final SongService songService;

    public PlayerController(FavoriteService favoriteService,
            HistoryService historyService,
            SongService songService) {
        this.favoriteService = favoriteService;
        this.historyService = historyService;
        this.songService = songService;
    }

    @PostMapping("/history")
    public ResponseEntity<ApiResponse<Void>> recordPlay(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long songId) {
        historyService.recordPlay(userDetails.getUsername(), songId);
        songService.incrementPlayCount(songId);
        return ResponseEntity.ok(ApiResponse.success("Play recorded", null));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SongResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SongResponse> response = historyService.getRecentlyPlayed(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long songId) {
        boolean favorited = favoriteService.toggleFavorite(userDetails.getUsername(), songId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("favorited", favorited)));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<SongResponse>>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SongResponse> response = favoriteService.getUserFavorites(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/favorites/check/{songId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long songId) {
        boolean favorited = favoriteService.isFavorited(userDetails.getUsername(), songId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("favorited", favorited)));
    }
}
