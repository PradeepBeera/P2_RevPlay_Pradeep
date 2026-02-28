package com.rev.app.service.impl;

import com.rev.app.dto.request.PlaylistRequest;
import com.rev.app.dto.response.PlaylistResponse;
import com.rev.app.entity.*;
import com.rev.app.entity.enums.PlaylistPrivacy;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.UnauthorizedException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.PlaylistRepository;
import com.rev.app.repository.PlaylistSongRepository;
import com.rev.app.repository.PlaylistFollowerRepository;
import com.rev.app.repository.SongRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.PlaylistService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private static final Logger logger = LogManager.getLogger(PlaylistServiceImpl.class);

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistFollowerRepository followerRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final EntityDtoMapper mapper;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository,
            PlaylistSongRepository playlistSongRepository,
            PlaylistFollowerRepository followerRepository,
            SongRepository songRepository,
            UserRepository userRepository,
            EntityDtoMapper mapper) {
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.followerRepository = followerRepository;
        this.songRepository = songRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PlaylistResponse createPlaylist(String email, PlaylistRequest request) {
        User user = findUserByEmail(email);

        PlaylistPrivacy privacy = PlaylistPrivacy.PRIVATE;
        if (request.getPrivacy() != null) {
            try {
                privacy = PlaylistPrivacy.valueOf(request.getPrivacy().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException("Invalid privacy value", HttpStatus.BAD_REQUEST);
            }
        }

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .privacy(privacy)
                .user(user)
                .build();

        playlist = playlistRepository.save(playlist);
        logger.info("Playlist created: {} by user: {}", playlist.getName(), email);
        return mapper.toPlaylistResponseSummary(playlist);
    }

    @Override
    public PlaylistResponse getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "id", id));

        List<Song> songs = playlistSongRepository.findByPlaylistIdOrderByOrderIndexAsc(id)
                .stream()
                .map(PlaylistSong::getSong)
                .collect(Collectors.toList());

        return mapper.toPlaylistResponse(playlist, songs);
    }

    @Override
    public List<PlaylistResponse> getUserPlaylists(String email) {
        User user = findUserByEmail(email);
        return playlistRepository.findByUserId(user.getId()).stream()
                .map(mapper::toPlaylistResponseSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlaylistResponse addSongToPlaylist(Long playlistId, Long songId, String email) {
        Playlist playlist = verifyPlaylistOwnership(playlistId, email);

        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)) {
            throw new CustomException("Song already in playlist", HttpStatus.CONFLICT);
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        Long currentCount = playlistSongRepository.countByPlaylistId(playlistId);

        PlaylistSong playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .orderIndex(currentCount.intValue())
                .build();

        playlistSongRepository.save(playlistSong);
        logger.info("Song {} added to playlist {}", songId, playlistId);
        return getPlaylistById(playlistId);
    }

    @Override
    @Transactional
    public PlaylistResponse removeSongFromPlaylist(Long playlistId, Long songId, String email) {
        verifyPlaylistOwnership(playlistId, email);
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
        logger.info("Song {} removed from playlist {}", songId, playlistId);
        return getPlaylistById(playlistId);
    }

    @Override
    @Transactional
    public PlaylistResponse reorderPlaylist(Long playlistId, List<Long> songIds, String email) {
        verifyPlaylistOwnership(playlistId, email);

        AtomicInteger index = new AtomicInteger(0);
        songIds.forEach(songId -> {
            playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                    .ifPresent(ps -> {
                        ps.setOrderIndex(index.getAndIncrement());
                        playlistSongRepository.save(ps);
                    });
        });

        logger.info("Playlist {} reordered", playlistId);
        return getPlaylistById(playlistId);
    }

    @Override
    @Transactional
    public void deletePlaylist(Long playlistId, String email) {
        verifyPlaylistOwnership(playlistId, email);
        playlistRepository.deleteById(playlistId);
        logger.info("Playlist {} deleted", playlistId);
    }

    @Override
    @Transactional
    public PlaylistResponse followPlaylist(Long playlistId, String email) {
        User user = findUserByEmail(email);
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "id", playlistId));

        if (playlist.getUser().getId().equals(user.getId())) {
            throw new CustomException("You cannot follow your own playlist", HttpStatus.BAD_REQUEST);
        }

        if (followerRepository.findByUserIdAndPlaylistId(user.getId(), playlistId).isPresent()) {
            throw new CustomException("Already following this playlist", HttpStatus.CONFLICT);
        }

        PlaylistFollower follower = PlaylistFollower.builder()
                .user(user)
                .playlist(playlist)
                .build();
        followerRepository.save(follower);
        return getPlaylistById(playlistId);
    }

    @Override
    @Transactional
    public void unfollowPlaylist(Long playlistId, String email) {
        User user = findUserByEmail(email);
        PlaylistFollower follower = followerRepository.findByUserIdAndPlaylistId(user.getId(), playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("PlaylistFollower", "playlistId", playlistId));
        followerRepository.delete(follower);
    }

    @Override
    public List<PlaylistResponse> getFollowedPlaylists(String email) {
        User user = findUserByEmail(email);
        return followerRepository.findByUserId(user.getId()).stream()
                .map(f -> mapper.toPlaylistResponseSummary(f.getPlaylist()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFollowing(Long playlistId, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            return false;
        return followerRepository.findByUserIdAndPlaylistId(user.getId(), playlistId).isPresent();
    }

    private Playlist verifyPlaylistOwnership(Long playlistId, String email) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "id", playlistId));
        User user = findUserByEmail(email);

        if (!playlist.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to modify this playlist");
        }
        return playlist;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
