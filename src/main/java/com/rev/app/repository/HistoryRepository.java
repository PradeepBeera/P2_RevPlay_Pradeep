package com.rev.app.repository;

import com.rev.app.entity.ListeningHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<ListeningHistory, Long> {

        List<ListeningHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

        @Query("SELECT h.user.id, COUNT(h) FROM ListeningHistory h WHERE h.song.artistProfile.id = :artistId " +
                        "GROUP BY h.user.id ORDER BY COUNT(h) DESC")
        List<Object[]> findTopListenersByArtist(@Param("artistId") Long artistId, Pageable pageable);

        @Query(value = "SELECT TO_CHAR(played_at, 'YYYY-MM-DD') as play_date, COUNT(*) as plays " +
                        "FROM listening_history h " +
                        "JOIN songs s ON h.song_id = s.id " +
                        "WHERE s.artist_id = :artistId AND h.played_at >= CURRENT_DATE - :days " +
                        "GROUP BY TO_CHAR(played_at, 'YYYY-MM-DD') ORDER BY play_date ASC", nativeQuery = true)
        List<Object[]> getPlayTrends(@Param("artistId") Long artistId, @Param("days") int days);
}
