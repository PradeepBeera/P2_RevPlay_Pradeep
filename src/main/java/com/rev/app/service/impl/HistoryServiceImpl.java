package com.rev.app.service.impl;

import com.rev.app.dto.response.SongResponse;
import com.rev.app.entity.ListeningHistory;
import com.rev.app.entity.Song;
import com.rev.app.entity.User;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.HistoryRepository;
import com.rev.app.repository.SongRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.HistoryService;
import com.rev.app.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl implements HistoryService {

    private static final Logger logger = LogManager.getLogger(HistoryServiceImpl.class);

    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final EntityDtoMapper mapper;

    public HistoryServiceImpl(HistoryRepository historyRepository,
            UserRepository userRepository,
            SongRepository songRepository,
            EntityDtoMapper mapper) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void recordPlay(String email, Long songId) {
        User user = findUserByEmail(email);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        ListeningHistory history = ListeningHistory.builder()
                .user(user)
                .song(song)
                .playedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
        logger.debug("Play recorded: user={}, song={}", email, songId);
    }

    @Override
    public List<SongResponse> getRecentlyPlayed(String email) {
        User user = findUserByEmail(email);
        return historyRepository.findByUserIdOrderByPlayedAtDesc(
                user.getId(), PageRequest.of(0, Constants.MAX_HISTORY_SIZE))
                .stream()
                .map(h -> mapper.toSongResponse(h.getSong()))
                .collect(Collectors.toList());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
