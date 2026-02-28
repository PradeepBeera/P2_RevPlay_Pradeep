package com.rev.app.service.interfaces;

import com.rev.app.dto.response.AnalyticsResponse;

public interface AnalyticsService {

    AnalyticsResponse getArtistAnalytics(String email);

    AnalyticsResponse getListenerStats(String email);
}
