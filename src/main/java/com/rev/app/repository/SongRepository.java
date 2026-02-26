package com.rev.app.repository;

import com.rev.app.entity.Song;
import com.rev.app.entity.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    Page<Song> findByVisibility(Visibility visibility, Pageable pageable);

    List<Song> findByArtistProfileId(Long artistId);

    @Query("SELECT s FROM Song s WHERE s.genre.name = :genre AND s.visibility = :visibility")
    Page<Song> findByGenreAndVisibility(@Param("genre") String genre, @Param("visibility") Visibility visibility,
            Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.visibility = :visibility AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.genre.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Song> searchPublicSongs(@Param("query") String query, @Param("visibility") Visibility visibility,
            Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.artistProfile.id = :artistId ORDER BY s.playCount DESC")
    List<Song> findTopSongsByArtist(@Param("artistId") Long artistId, Pageable pageable);

    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :songId")
    void incrementPlayCount(@Param("songId") Long songId);

    @Query("SELECT COALESCE(SUM(s.playCount), 0) FROM Song s WHERE s.artistProfile.id = :artistId")
    Long getTotalPlaysByArtist(@Param("artistId") Long artistId);

    Long countByArtistProfileId(Long artistId);

    List<Song> findByAlbumId(Long albumId);
}
