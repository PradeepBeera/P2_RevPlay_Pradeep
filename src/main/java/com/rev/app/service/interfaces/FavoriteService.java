package com.rev.app.service.interfaces;

import com.rev.app.dto.response.SongResponse;

import java.util.List;

public interface FavoriteService {

    boolean toggleFavorite(String email, Long songId);

    List<SongResponse> getUserFavorites(String email);

    boolean isFavorited(String email, Long songId);
}
