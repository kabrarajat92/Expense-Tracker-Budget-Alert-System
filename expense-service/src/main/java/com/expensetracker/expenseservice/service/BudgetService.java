package com.expensetracker.expenseservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.expensetracker.expenseservice.dto.BudgetRequest;
import com.expensetracker.expenseservice.entity.Budget;
import com.expensetracker.expenseservice.repository.BudgetRepository;

@Service
public class BudgetService {

	private final BudgetRepository budgetRepository;

	public BudgetService(BudgetRepository budgetRepository) {
		super();
		this.budgetRepository = budgetRepository;
	}
	
	public Budget createOrUpdateBudget(String username, BudgetRequest request) {
        Budget budget = budgetRepository.findByUsernameAndCategory(username, request.getCategory())
                .orElse(new Budget());

        budget.setUsername(username);
        budget.setCategory(request.getCategory());
        budget.setLimitAmount(request.getLimitAmount());

        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsForUser(String username) {
        return budgetRepository.findByUsername(username);
    }
}
