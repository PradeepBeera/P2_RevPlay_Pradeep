package com.rev.app.controller;

import com.rev.app.dto.response.AnalyticsResponse;
import com.rev.app.dto.response.ApiResponse;
import com.rev.app.service.interfaces.AnalyticsService;
import com.rev.app.util.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.ANALYTICS_PREFIX)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getArtistDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        AnalyticsResponse response = analyticsService.getArtistAnalytics(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getMyStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        AnalyticsResponse response = analyticsService.getListenerStats(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
