package com.expensetracker.expenseservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.expenseservice.dto.AiAnalysisResponse;
import com.expensetracker.expenseservice.dto.BudgetSuggestionRequest;
import com.expensetracker.expenseservice.dto.BudgetSuggestionResponse;
import com.expensetracker.expenseservice.dto.ChatRequest;
import com.expensetracker.expenseservice.dto.ChatResponse;
import com.expensetracker.expenseservice.dto.ForecastResponse;
import com.expensetracker.expenseservice.repository.BudgetRepository;
import com.expensetracker.expenseservice.repository.ExpenseRepository;
import com.expensetracker.expenseservice.service.GroqAiService;

@RestController
@RequestMapping("/api/ai")
public class AiController {
	private final GroqAiService groqAiService;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    
	public AiController(GroqAiService groqAiService, ExpenseRepository expenseRepository,
			BudgetRepository budgetRepository) {
		super();
		this.groqAiService = groqAiService;
		this.expenseRepository = expenseRepository;
		this.budgetRepository = budgetRepository;
	}
	
	@GetMapping("/analyze")
	public AiAnalysisResponse analyze(Authentication authentication) throws Exception {
	    String username = authentication.getName();
	    var expenses = expenseRepository.findByUsername(username);
	    var budgets = budgetRepository.findByUsername(username);
	    String result = groqAiService.analyzeSpendingCached(username, expenses, budgets);
	    return new AiAnalysisResponse(result);
	}

	@PostMapping("/suggest-budget")
	public BudgetSuggestionResponse suggestBudget(@RequestBody BudgetSuggestionRequest request,
	                                                Authentication authentication) throws Exception {
	    String username = authentication.getName();
	    var expenses = expenseRepository.findByUsername(username);
	    String result = groqAiService.suggestBudgetAsync(expenses, request.getMonthlyIncome()).get();
	    return new BudgetSuggestionResponse(result);
	}

	@GetMapping("/forecast")
	public ForecastResponse forecast(Authentication authentication) throws Exception {
	    String username = authentication.getName();
	    var expenses = expenseRepository.findByUsername(username);
	    var budgets = budgetRepository.findByUsername(username);
	    String result = groqAiService.forecastSpendingAsync(expenses, budgets).get();
	    return new ForecastResponse(result);
	}

	@PostMapping("/chat")
	public ChatResponse chat(@RequestBody ChatRequest request, Authentication authentication) throws Exception {
	    String username = authentication.getName();
	    var expenses = expenseRepository.findByUsername(username);
	    var budgets = budgetRepository.findByUsername(username);
	    String result = groqAiService.chatAsync(request.getMessage(), expenses, budgets).get();
	    return new ChatResponse(result);
	}
    
    
}
