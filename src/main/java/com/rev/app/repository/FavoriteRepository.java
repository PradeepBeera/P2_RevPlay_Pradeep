package com.rev.app.repository;

import com.rev.app.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    Optional<Favorite> findByUserIdAndSongId(Long userId, Long songId);

    boolean existsByUserIdAndSongId(Long userId, Long songId);

    void deleteByUserIdAndSongId(Long userId, Long songId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.song.artistProfile.id = :artistId")
    Long countFavoritesByArtist(@Param("artistId") Long artistId);

    Long countBySongId(Long songId);
}
