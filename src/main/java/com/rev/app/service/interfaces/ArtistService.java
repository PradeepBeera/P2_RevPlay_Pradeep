package com.rev.app.service.interfaces;

import com.rev.app.dto.request.ArtistProfileRequest;
import com.rev.app.dto.response.ArtistProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArtistService {

    ArtistProfileResponse createProfile(String email, ArtistProfileRequest request);

    ArtistProfileResponse getProfileByUserId(Long userId);

    ArtistProfileResponse getProfileById(Long artistId);

    List<ArtistProfileResponse> getAllArtists();

    List<ArtistProfileResponse> searchArtists(String query);

    ArtistProfileResponse updateBanner(String email, MultipartFile file);
}
