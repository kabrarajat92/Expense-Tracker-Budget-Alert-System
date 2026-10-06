package com.expensetracker.analyticsservice.controller;

import com.expensetracker.analyticsservice.dto.SpendingAnalytics;
import com.expensetracker.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/spending-summary")
    public ResponseEntity<SpendingAnalytics> getSpendingSummary(
            @RequestParam(defaultValue = "2026-10") String monthYear,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(analyticsService.getMonthlyAnalytics(username, monthYear));
    }
}
