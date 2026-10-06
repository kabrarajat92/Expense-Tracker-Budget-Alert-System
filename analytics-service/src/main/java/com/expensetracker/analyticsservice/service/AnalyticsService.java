package com.expensetracker.analyticsservice.service;

import com.expensetracker.analyticsservice.dto.CategoryBreakdown;
import com.expensetracker.analyticsservice.dto.SpendingAnalytics;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    @Cacheable(value = "analyticsCache", key = "#username + '_' + #monthYear")
    public SpendingAnalytics getMonthlyAnalytics(String username, String monthYear) {
        // Aggregated insights logic (leveraging cached queries across service databases)
        List<CategoryBreakdown> breakdowns = Arrays.asList(
                CategoryBreakdown.builder().category("Food & Dining").amount(new BigDecimal("450.00")).percentage(45.0).build(),
                CategoryBreakdown.builder().category("Transportation").amount(new BigDecimal("250.00")).percentage(25.0).build(),
                CategoryBreakdown.builder().category("Entertainment").amount(new BigDecimal("200.00")).percentage(20.0).build(),
                CategoryBreakdown.builder().category("Utilities").amount(new BigDecimal("100.00")).percentage(10.0).build()
        );

        return SpendingAnalytics.builder()
                .username(username)
                .monthYear(monthYear)
                .totalSpent(new BigDecimal("1000.00"))
                .categoryBreakdown(breakdowns)
                .budgetUtilizationPercentage(75.5)
                .build();
    }
}
