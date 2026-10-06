package com.expensetracker.expenseservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.expensetracker.expenseservice.dto.ExpenseRequest;
import com.expensetracker.expenseservice.entity.Expense;
import com.expensetracker.expenseservice.event.BudgetAlertEvent;
import com.expensetracker.expenseservice.repository.BudgetRepository;
import com.expensetracker.expenseservice.repository.ExpenseRepository;

@Service
public class ExpenseService {
	private final ExpenseRepository expenseRepository;
	private final BudgetRepository budgetRepository;
    private final KafkaProducerService kafkaProducerService;
    private final GroqAiService groqAiService;
	
	public ExpenseService(ExpenseRepository expenseRepository, BudgetRepository budgetRepository,
			KafkaProducerService kafkaProducerService, GroqAiService groqAiService) {
		super();
		this.expenseRepository = expenseRepository;
		this.budgetRepository = budgetRepository;
		this.kafkaProducerService = kafkaProducerService;
		this.groqAiService = groqAiService;
	}

	public Expense addExpense(String username, String email, ExpenseRequest request) {
	    Expense expense = new Expense();
	    expense.setUsername(username);
	    expense.setCategory(request.getCategory());
	    expense.setAmount(request.getAmount());
	    expense.setDescription(request.getDescription());
	    expense.setExpenseDate(request.getExpenseDate());

	    Expense saved = expenseRepository.save(expense);
	    groqAiService.evictAnalysisCache(username);
	    checkBudgetThreshold(username, email, request.getCategory());

	    return saved;
	}

	private void checkBudgetThreshold(String username, String email, String category) {
	    budgetRepository.findByUsernameAndCategory(username, category).ifPresent(budget -> {
	        List<Expense> expenses = expenseRepository.findByUsernameAndCategory(username, category);

	        BigDecimal totalSpent = expenses.stream()
	                .map(Expense::getAmount)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	        double percentageUsed = totalSpent
	                .divide(budget.getLimitAmount(), 4, java.math.RoundingMode.HALF_UP)
	                .doubleValue() * 100;

	        if (percentageUsed >= 80.0) {
	            BudgetAlertEvent event = new BudgetAlertEvent(
	                    username, email, category, budget.getLimitAmount(), totalSpent, percentageUsed
	            );
	            kafkaProducerService.sendBudgetAlert(event);
	        }
	    });
	}

    public List<Expense> getExpensesForUser(String username) {
        return expenseRepository.findByUsername(username);
    }
	
}
