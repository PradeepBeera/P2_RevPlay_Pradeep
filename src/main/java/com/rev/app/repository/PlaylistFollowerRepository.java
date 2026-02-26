package com.rev.app.repository;

import com.rev.app.entity.PlaylistFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistFollowerRepository extends JpaRepository<PlaylistFollower, Long> {
    Optional<PlaylistFollower> findByUserIdAndPlaylistId(Long userId, Long playlistId);

    List<PlaylistFollower> findByUserId(Long userId);

    Long countByPlaylistId(Long playlistId);
}
