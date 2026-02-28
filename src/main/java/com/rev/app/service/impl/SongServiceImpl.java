package com.rev.app.service.impl;

import com.rev.app.dto.request.SongUploadRequest;
import com.rev.app.dto.response.SongResponse;
import com.rev.app.entity.Album;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.Genre;
import com.rev.app.entity.Song;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Visibility;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.UnauthorizedException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.*;
import com.rev.app.service.interfaces.SongService;
import com.rev.app.util.FileStorageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class SongServiceImpl implements SongService {

    private static final Logger logger = LogManager.getLogger(SongServiceImpl.class);

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final FileStorageUtil fileStorageUtil;
    private final EntityDtoMapper mapper;

    public SongServiceImpl(SongRepository songRepository,
            ArtistRepository artistRepository,
            AlbumRepository albumRepository,
            UserRepository userRepository,
            GenreRepository genreRepository,
            FileStorageUtil fileStorageUtil,
            EntityDtoMapper mapper) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.fileStorageUtil = fileStorageUtil;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public SongResponse uploadSong(String email, SongUploadRequest request,
            MultipartFile audioFile, MultipartFile coverImage) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        ArtistProfile artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new CustomException("Artist profile required to upload songs", HttpStatus.FORBIDDEN));

        String audioUrl = fileStorageUtil.storeAudioFile(audioFile);
        String coverUrl = (coverImage != null && !coverImage.isEmpty())
                ? fileStorageUtil.storeImageFile(coverImage)
                : null;

        Genre genreEntity = null;
        if (request.getGenre() != null && !request.getGenre().isBlank()) {
            genreEntity = genreRepository.findByName(request.getGenre())
                    .orElseGet(() -> genreRepository.save(new Genre(null, request.getGenre())));
        }

        Album album = null;
        if (request.getAlbumId() != null) {
            album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album", "id", request.getAlbumId()));
        }

        Visibility visibility = Visibility.PUBLIC;
        if (request.getVisibility() != null) {
            try {
                visibility = Visibility.valueOf(request.getVisibility().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException("Invalid visibility value", HttpStatus.BAD_REQUEST);
            }
        }

        Song song = Song.builder()
                .title(request.getTitle())
                .genre(genreEntity)
                .duration(request.getDuration())
                .releaseDate(
                        request.getReleaseDate() != null ? LocalDate.parse(request.getReleaseDate()) : LocalDate.now())
                .audioUrl(audioUrl)
                .coverImage(coverUrl)
                .visibility(visibility)
                .playCount(0L)
                .fileSize(audioFile.getSize())
                .status("PUBLISHED")
                .artistProfile(artist)
                .album(album)
                .build();

        song = songRepository.save(song);
        logger.info("Song uploaded: {} by artist: {}", song.getTitle(), artist.getArtistName());

        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .genre(song.getGenre() != null ? song.getGenre().getName() : null)
                .duration(song.getDuration())
                .releaseDate(song.getReleaseDate())
                .audioUrl(song.getAudioUrl())
                .coverImage(song.getCoverImage())
                .visibility(song.getVisibility().name())
                .status(song.getStatus())
                .fileSize(song.getFileSize())
                .playCount(song.getPlayCount())
                .artistName(artist.getArtistName())
                .artistId(artist.getId())
                .albumName(album != null ? album.getName() : null)
                .albumId(album != null ? album.getId() : null)
                .build();
    }

    @Override
    public SongResponse getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));
        return mapper.toSongResponse(song);
    }

    @Override
    public Page<SongResponse> getPublicSongs(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseDate"));
        return songRepository.findByVisibility(Visibility.PUBLIC, pageRequest)
                .map(mapper::toSongResponse);
    }

    @Override
    public Page<SongResponse> searchSongs(String query, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return songRepository.searchPublicSongs(query, Visibility.PUBLIC, pageRequest)
                .map(mapper::toSongResponse);
    }

    @Override
    public Page<SongResponse> getSongsByGenre(String genre, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "playCount"));
        return songRepository.findByGenreAndVisibility(genre, Visibility.PUBLIC, pageRequest)
                .map(mapper::toSongResponse);
    }

    @Override
    public List<SongResponse> getSongsByArtist(Long artistId) {
        return mapper.toSongResponseList(songRepository.findByArtistProfileId(artistId));
    }

    @Override
    public List<SongResponse> getSongsByAlbum(Long albumId) {
        return mapper.toSongResponseList(songRepository.findByAlbumId(albumId));
    }

    @Override
    @Transactional
    public void incrementPlayCount(Long songId) {
        songRepository.incrementPlayCount(songId);
    }

    @Override
    @Transactional
    public void deleteSong(Long songId, String email) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!song.getArtistProfile().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this song");
        }

        songRepository.delete(song);
        logger.info("Song deleted: {}", song.getTitle());
    }
}
