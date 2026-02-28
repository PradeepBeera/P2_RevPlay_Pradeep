package com.rev.app.service.interfaces;

import com.rev.app.dto.response.SongResponse;

import java.util.List;

public interface HistoryService {

    void recordPlay(String email, Long songId);

    List<SongResponse> getRecentlyPlayed(String email);
}
