package com.rev.app.repository;

import com.rev.app.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    List<PlaylistSong> findByPlaylistIdOrderByOrderIndexAsc(Long playlistId);

    Optional<PlaylistSong> findByPlaylistIdAndSongId(Long playlistId, Long songId);

    boolean existsByPlaylistIdAndSongId(Long playlistId, Long songId);

    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);

    Long countByPlaylistId(Long playlistId);
}
