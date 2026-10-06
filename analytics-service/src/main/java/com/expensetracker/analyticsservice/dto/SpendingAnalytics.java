package com.expensetracker.analyticsservice.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingAnalytics implements Serializable {
    private String username;
    private String monthYear;
    private BigDecimal totalSpent;
    private List<CategoryBreakdown> categoryBreakdown;
    private Double budgetUtilizationPercentage;
}
