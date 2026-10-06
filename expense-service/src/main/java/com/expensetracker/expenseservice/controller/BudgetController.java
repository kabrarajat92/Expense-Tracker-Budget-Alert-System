package com.expensetracker.expenseservice.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.expenseservice.dto.BudgetRequest;
import com.expensetracker.expenseservice.entity.Budget;
import com.expensetracker.expenseservice.service.BudgetService;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
	
	private final BudgetService budgetService;

	public BudgetController(BudgetService budgetService) {
		super();
		this.budgetService = budgetService;
	}
	
	@PostMapping
    public Budget setBudget(@RequestBody BudgetRequest request, Authentication authentication) {
        return budgetService.createOrUpdateBudget(authentication.getName(), request);
    }

    @GetMapping
    public List<Budget> getBudgets(Authentication authentication) {
        return budgetService.getBudgetsForUser(authentication.getName());
    }
	

}
