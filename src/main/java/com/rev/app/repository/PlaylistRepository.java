package com.rev.app.repository;

import com.rev.app.entity.Playlist;
import com.rev.app.entity.enums.PlaylistPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUserId(Long userId);

    List<Playlist> findByPrivacy(PlaylistPrivacy privacy);
}
