package com.rev.app.controller;

import com.rev.app.dto.request.SongUploadRequest;
import com.rev.app.dto.response.ApiResponse;
import com.rev.app.dto.response.SongResponse;
import com.rev.app.service.interfaces.SongService;
import com.rev.app.util.Constants;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(Constants.SONGS_PREFIX)
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SongResponse>> uploadSong(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute SongUploadRequest request,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        SongResponse response = songService.uploadSong(userDetails.getUsername(), request, audioFile, coverImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Song uploaded", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SongResponse>> getSongById(@PathVariable Long id) {
        SongResponse response = songService.getSongById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SongResponse>>> getPublicSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SongResponse> response = songService.getPublicSongs(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<SongResponse>>> searchSongs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SongResponse> response = songService.searchSongs(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<Page<SongResponse>>> getSongsByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SongResponse> response = songService.getSongsByGenre(genre, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/play")
    public ResponseEntity<ApiResponse<Void>> incrementPlayCount(@PathVariable Long id) {
        songService.incrementPlayCount(id);
        return ResponseEntity.ok(ApiResponse.success("Play count incremented", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSong(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        songService.deleteSong(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Song deleted", null));
    }
}
