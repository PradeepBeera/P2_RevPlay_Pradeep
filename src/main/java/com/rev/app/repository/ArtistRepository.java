package com.rev.app.repository;

import com.rev.app.entity.ArtistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<ArtistProfile, Long> {

    @Query("SELECT a FROM ArtistProfile a JOIN FETCH a.user WHERE a.user.id = :userId")
    Optional<ArtistProfile> findByUserId(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT a FROM ArtistProfile a WHERE LOWER(a.artistName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ArtistProfile> searchByName(@Param("name") String name);
}
