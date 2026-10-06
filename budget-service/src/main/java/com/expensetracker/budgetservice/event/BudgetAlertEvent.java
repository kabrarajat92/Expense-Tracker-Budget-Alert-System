package com.expensetracker.budgetservice.event;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlertEvent {
    private String username;
    private String category;
    private BigDecimal monthlyLimit;
    private BigDecimal currentSpent;
    private BigDecimal percentageUsed;
    private String monthYear;
    private String userEmail;
}
