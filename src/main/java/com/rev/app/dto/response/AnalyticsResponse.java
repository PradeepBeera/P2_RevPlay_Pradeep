package com.rev.app.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {

    private Long totalSongs;
    private Long totalPlays;
    private Long totalFavorites;
    private List<SongResponse> topSongs;
    private List<TopListenerResponse> topListeners;
    private List<PlayTrend> dailyTrends;
    private ListenerStats listenerStats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayTrend {
        private String label;
        private Long value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListenerStats {
        private Long totalListeningTime; // In minutes
        private Long favoriteGenresCount;
        private List<String> topGenres;
        private Long followedPlaylistsCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopListenerResponse {
        private Long userId;
        private String username;
        private Long playCount;
    }
}
