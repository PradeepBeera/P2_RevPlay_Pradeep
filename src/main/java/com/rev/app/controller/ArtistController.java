package com.rev.app.controller;

import com.rev.app.dto.request.ArtistProfileRequest;
import com.rev.app.dto.response.ApiResponse;
import com.rev.app.dto.response.ArtistProfileResponse;
import com.rev.app.dto.response.SongResponse;
import com.rev.app.service.interfaces.ArtistService;
import com.rev.app.service.interfaces.SongService;
import com.rev.app.util.Constants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import java.util.List;

@RestController
@RequestMapping(Constants.ARTISTS_PREFIX)
public class ArtistController {

    private final ArtistService artistService;
    private final SongService songService;

    public ArtistController(ArtistService artistService, SongService songService) {
        this.artistService = artistService;
        this.songService = songService;
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<ArtistProfileResponse>> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ArtistProfileRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ArtistProfileResponse response = artistService.createProfile(userDetails.getUsername(), request);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            java.util.List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
            updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_ARTIST"));
            Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
                    updatedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
            securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpServletRequest,
                    httpServletResponse);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Artist profile created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArtistProfileResponse>>> getAllArtists() {
        List<ArtistProfileResponse> response = artistService.getAllArtists();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtistProfileResponse>> getArtistById(@PathVariable Long id) {
        ArtistProfileResponse response = artistService.getProfileById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<List<SongResponse>>> getArtistSongs(@PathVariable Long id) {
        List<SongResponse> response = songService.getSongsByArtist(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ArtistProfileResponse>>> searchArtists(@RequestParam String query) {
        List<ArtistProfileResponse> response = artistService.searchArtists(query);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/banner")
    public ResponseEntity<ApiResponse<ArtistProfileResponse>> updateBanner(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        ArtistProfileResponse response = artistService.updateBanner(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success("Banner updated", response));
    }
}
