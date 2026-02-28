package com.rev.app.service.impl;

import com.rev.app.dto.response.AnalyticsResponse;
import com.rev.app.dto.response.SongResponse;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.ListeningHistory;
import com.rev.app.entity.User;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.*;
import com.rev.app.service.interfaces.AnalyticsService;
import com.rev.app.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

        private static final Logger logger = LogManager.getLogger(AnalyticsServiceImpl.class);

        private final SongRepository songRepository;
        private final FavoriteRepository favoriteRepository;
        private final HistoryRepository historyRepository;
        private final ArtistRepository artistRepository;
        private final UserRepository userRepository;
        private final PlaylistFollowerRepository playlistFollowerRepository;
        private final EntityDtoMapper mapper;

        public AnalyticsServiceImpl(SongRepository songRepository,
                        FavoriteRepository favoriteRepository,
                        HistoryRepository historyRepository,
                        ArtistRepository artistRepository,
                        UserRepository userRepository,
                        PlaylistFollowerRepository playlistFollowerRepository,
                        EntityDtoMapper mapper) {
                this.songRepository = songRepository;
                this.favoriteRepository = favoriteRepository;
                this.historyRepository = historyRepository;
                this.artistRepository = artistRepository;
                this.userRepository = userRepository;
                this.playlistFollowerRepository = playlistFollowerRepository;
                this.mapper = mapper;
        }

        @Override
        @Transactional(readOnly = true)
        public AnalyticsResponse getArtistAnalytics(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

                ArtistProfile artist = artistRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new CustomException("Artist profile not found",
                                                HttpStatus.NOT_FOUND));

                Long artistId = artist.getId();
                Long totalSongs = songRepository.countByArtistProfileId(artistId);
                Long totalPlays = songRepository.getTotalPlaysByArtist(artistId);
                Long totalFavorites = favoriteRepository.countFavoritesByArtist(artistId);

                List<SongResponse> topSongs = songRepository.findTopSongsByArtist(
                                artistId, PageRequest.of(0, Constants.TOP_SONGS_LIMIT))
                                .stream()
                                .map(mapper::toSongResponse)
                                .collect(Collectors.toList());

                List<AnalyticsResponse.TopListenerResponse> topListeners = historyRepository
                                .findTopListenersByArtist(artistId, PageRequest.of(0, Constants.TOP_LISTENERS_LIMIT))
                                .stream()
                                .map(row -> {
                                        Long userId = convertToLong(row[0]);
                                        Long playCount = convertToLong(row[1]);
                                        String username = userRepository.findById(userId)
                                                        .map(User::getUsername)
                                                        .orElse("Unknown");
                                        return AnalyticsResponse.TopListenerResponse.builder()
                                                        .userId(userId)
                                                        .username(username)
                                                        .playCount(playCount)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                List<AnalyticsResponse.PlayTrend> dailyTrends = historyRepository
                                .getPlayTrends(artistId, 7) // Last 7 days
                                .stream()
                                .map(row -> AnalyticsResponse.PlayTrend.builder()
                                                .label((String) row[0])
                                                .value(convertToLong(row[1]))
                                                .build())
                                .collect(Collectors.toList());

                logger.info("Analytics retrieved for artist: {}", artist.getArtistName());

                return AnalyticsResponse.builder()
                                .totalSongs(totalSongs)
                                .totalPlays(totalPlays)
                                .totalFavorites(totalFavorites)
                                .topSongs(topSongs)
                                .topListeners(topListeners)
                                .dailyTrends(dailyTrends)
                                .build();
        }

        /**
         * Safely converts an object (usually from a database row) to a Long.
         * Oracle often returns BigDecimal for numeric results in native queries.
         */
        private Long convertToLong(Object val) {
                if (val == null)
                        return 0L;
                if (val instanceof Long)
                        return (Long) val;
                if (val instanceof Number)
                        return ((Number) val).longValue();
                try {
                        return Long.valueOf(val.toString());
                } catch (Exception e) {
                        return 0L;
                }
        }

        @Override
        @Transactional(readOnly = true)
        public AnalyticsResponse getListenerStats(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

                Long userId = user.getId();
                List<ListeningHistory> history = historyRepository.findByUserIdOrderByPlayedAtDesc(userId,
                                PageRequest.of(0, 1000));

                Long totalPlays = (long) history.size();
                Long totalListeningTime = totalPlays * 3; // Estimated 3 mins per song

                Map<String, Long> genreCounts = history.stream()
                                .filter(h -> h.getSong().getGenre() != null)
                                .collect(Collectors.groupingBy(h -> h.getSong().getGenre().getName(),
                                                Collectors.counting()));

                List<String> topGenres = genreCounts.entrySet().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(3)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                Long followedPlaylistsCount = (long) playlistFollowerRepository.findByUserId(userId).size();

                return AnalyticsResponse.builder()
                                .totalPlays(totalPlays)
                                .listenerStats(AnalyticsResponse.ListenerStats.builder()
                                                .totalListeningTime(totalListeningTime)
                                                .favoriteGenresCount((long) genreCounts.size())
                                                .topGenres(topGenres)
                                                .followedPlaylistsCount(followedPlaylistsCount)
                                                .build())
                                .build();
        }
}
