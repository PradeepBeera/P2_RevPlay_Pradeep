package com.rev.app.service.impl;

import com.rev.app.dto.response.SongResponse;
import com.rev.app.entity.Favorite;
import com.rev.app.entity.Song;
import com.rev.app.entity.User;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.FavoriteRepository;
import com.rev.app.repository.SongRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.FavoriteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger logger = LogManager.getLogger(FavoriteServiceImpl.class);

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final EntityDtoMapper mapper;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            SongRepository songRepository,
            EntityDtoMapper mapper) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(String email, Long songId) {
        User user = findUserByEmail(email);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndSongId(user.getId(), songId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            logger.info("Song {} unfavorited by user {}", songId, email);
            return false;
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .song(song)
                .build();
        favoriteRepository.save(favorite);
        logger.info("Song {} favorited by user {}", songId, email);
        return true;
    }

    @Override
    public List<SongResponse> getUserFavorites(String email) {
        User user = findUserByEmail(email);
        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(fav -> mapper.toSongResponse(fav.getSong()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavorited(String email, Long songId) {
        User user = findUserByEmail(email);
        return favoriteRepository.existsByUserIdAndSongId(user.getId(), songId);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
