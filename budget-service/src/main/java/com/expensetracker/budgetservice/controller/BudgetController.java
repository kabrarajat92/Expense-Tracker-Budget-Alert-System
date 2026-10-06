package com.expensetracker.budgetservice.controller;

import com.expensetracker.budgetservice.entity.Budget;
import com.expensetracker.budgetservice.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<Budget> setBudget(@RequestBody Budget budget, Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(budgetService.setBudget(username, budget));
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getUserBudgets(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(budgetService.getUserBudgets(username));
    }

    @PostMapping("/check-threshold")
    public ResponseEntity<Void> checkThreshold(
            @RequestParam String category,
            @RequestParam BigDecimal currentSpent,
            @RequestParam String monthYear,
            @RequestParam(required = false) String userEmail,
            Authentication authentication) {
        String username = authentication.getName();
        budgetService.checkAndTriggerAlert(username, category, currentSpent, monthYear, userEmail);
        return ResponseEntity.ok().build();
    }
}
