package com.rev.app.controller;

import com.rev.app.dto.request.AlbumRequest;
import com.rev.app.dto.response.AlbumResponse;
import com.rev.app.dto.response.ApiResponse;
import com.rev.app.service.interfaces.AlbumService;
import com.rev.app.util.Constants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(Constants.ALBUMS_PREFIX)
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute AlbumRequest request,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        AlbumResponse response = albumService.createAlbum(userDetails.getUsername(), request, coverImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Album created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbumById(@PathVariable Long id) {
        AlbumResponse response = albumService.getAlbumById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<ApiResponse<List<AlbumResponse>>> getAlbumsByArtist(@PathVariable Long artistId) {
        List<AlbumResponse> response = albumService.getAlbumsByArtist(artistId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
