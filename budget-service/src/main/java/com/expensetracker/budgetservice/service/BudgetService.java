package com.expensetracker.budgetservice.service;

import com.expensetracker.budgetservice.entity.Budget;
import com.expensetracker.budgetservice.event.BudgetAlertEvent;
import com.expensetracker.budgetservice.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Budget setBudget(String username, Budget budget) {
        budget.setUsername(username);
        Optional<Budget> existing = budgetRepository.findByUsernameAndCategoryAndMonthYear(
                username, budget.getCategory(), budget.getMonthYear());

        if (existing.isPresent()) {
            Budget b = existing.get();
            b.setMonthlyLimit(budget.getMonthlyLimit());
            b.setAlertThresholdPercentage(budget.getAlertThresholdPercentage());
            return budgetRepository.save(b);
        }
        return budgetRepository.save(budget);
    }

    public List<Budget> getUserBudgets(String username) {
        return budgetRepository.findByUsername(username);
    }

    public void checkAndTriggerAlert(String username, String category, BigDecimal currentSpent, String monthYear, String userEmail) {
        Optional<Budget> budgetOpt = budgetRepository.findByUsernameAndCategoryAndMonthYear(username, category, monthYear);
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            BigDecimal percentageUsed = currentSpent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(budget.getMonthlyLimit(), 2, RoundingMode.HALF_UP);

            if (percentageUsed.compareTo(budget.getAlertThresholdPercentage()) >= 0) {
                BudgetAlertEvent event = BudgetAlertEvent.builder()
                        .username(username)
                        .category(category)
                        .monthlyLimit(budget.getMonthlyLimit())
                        .currentSpent(currentSpent)
                        .percentageUsed(percentageUsed)
                        .monthYear(monthYear)
                        .userEmail(userEmail)
                        .build();

                kafkaTemplate.send("budget-alerts", username, event);
            }
        }
    }
}
