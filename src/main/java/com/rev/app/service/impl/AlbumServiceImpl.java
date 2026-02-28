package com.rev.app.service.impl;

import com.rev.app.dto.request.AlbumRequest;
import com.rev.app.dto.response.AlbumResponse;
import com.rev.app.entity.Album;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.User;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.AlbumRepository;
import com.rev.app.repository.ArtistRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.AlbumService;
import com.rev.app.util.FileStorageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlbumServiceImpl implements AlbumService {

    private static final Logger logger = LogManager.getLogger(AlbumServiceImpl.class);

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;
    private final EntityDtoMapper mapper;

    public AlbumServiceImpl(AlbumRepository albumRepository,
            ArtistRepository artistRepository,
            UserRepository userRepository,
            FileStorageUtil fileStorageUtil,
            EntityDtoMapper mapper) {
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.fileStorageUtil = fileStorageUtil;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AlbumResponse createAlbum(String email, AlbumRequest request, MultipartFile coverImage) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ArtistProfile artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new CustomException("Artist profile required to create albums", HttpStatus.FORBIDDEN));

        String coverUrl = (coverImage != null && !coverImage.isEmpty())
                ? fileStorageUtil.storeImageFile(coverImage)
                : null;

        Album album = Album.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(
                        request.getReleaseDate() != null ? LocalDate.parse(request.getReleaseDate()) : LocalDate.now())
                .coverImage(coverUrl)
                .artistProfile(artist)
                .build();

        album = albumRepository.save(album);
        logger.info("Album created: {} by artist: {}", album.getName(), artist.getArtistName());
        return mapper.toAlbumResponse(album);
    }

    @Override
    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album", "id", id));
        return mapper.toAlbumResponse(album);
    }

    @Override
    public List<AlbumResponse> getAlbumsByArtist(Long artistId) {
        return albumRepository.findByArtistProfileId(artistId).stream()
                .map(mapper::toAlbumResponse)
                .collect(Collectors.toList());
    }
}
