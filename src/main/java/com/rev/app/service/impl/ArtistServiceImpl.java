package com.rev.app.service.impl;

import com.rev.app.dto.request.ArtistProfileRequest;
import com.rev.app.dto.response.ArtistProfileResponse;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.Genre;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.ArtistRepository;
import com.rev.app.repository.GenreRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.ArtistService;
import com.rev.app.util.FileStorageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtistServiceImpl implements ArtistService {

    private static final Logger logger = LogManager.getLogger(ArtistServiceImpl.class);

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final EntityDtoMapper mapper;
    private final FileStorageUtil fileStorageUtil;

    public ArtistServiceImpl(ArtistRepository artistRepository,
            UserRepository userRepository,
            GenreRepository genreRepository,
            EntityDtoMapper mapper,
            FileStorageUtil fileStorageUtil) {
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.mapper = mapper;
        this.fileStorageUtil = fileStorageUtil;
    }

    @Override
    @Transactional
    public ArtistProfileResponse createProfile(String email, ArtistProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (artistRepository.existsByUserId(user.getId())) {
            throw new CustomException("Artist profile already exists", HttpStatus.CONFLICT);
        }

        user.setRole(Role.ARTIST);
        userRepository.save(user);

        Genre genre = null;
        if (request.getGenre() != null && !request.getGenre().isBlank()) {
            genre = genreRepository.findByName(request.getGenre())
                    .orElseGet(() -> genreRepository.save(new Genre(null, request.getGenre())));
        }

        ArtistProfile profile = ArtistProfile.builder()
                .user(user)
                .artistName(request.getArtistName())
                .genre(genre)
                .instagramLink(request.getInstagramLink())
                .twitterLink(request.getTwitterLink())
                .youtubeLink(request.getYoutubeLink())
                .spotifyLink(request.getSpotifyLink())
                .websiteLink(request.getWebsiteLink())
                .build();

        profile = artistRepository.save(profile);
        logger.info("Artist profile created for user: {}", email);
        return mapper.toArtistProfileResponse(profile);
    }

    @Override
    public ArtistProfileResponse getProfileByUserId(Long userId) {
        ArtistProfile profile = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ArtistProfile", "userId", userId));
        return mapper.toArtistProfileResponse(profile);
    }

    @Override
    public ArtistProfileResponse getProfileById(Long artistId) {
        ArtistProfile profile = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("ArtistProfile", "id", artistId));
        return mapper.toArtistProfileResponse(profile);
    }

    @Override
    public List<ArtistProfileResponse> getAllArtists() {
        return artistRepository.findAll().stream()
                .map(mapper::toArtistProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArtistProfileResponse> searchArtists(String query) {
        return artistRepository.searchByName(query).stream()
                .map(mapper::toArtistProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ArtistProfileResponse updateBanner(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ArtistProfile profile = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ArtistProfile", "userId", user.getId()));

        String bannerUrl = fileStorageUtil.storeImageFile(file);
        profile.setBannerImage(bannerUrl);
        profile = artistRepository.save(profile);
        logger.info("Banner updated for artist: {}", profile.getArtistName());
        return mapper.toArtistProfileResponse(profile);
    }
}
