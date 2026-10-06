package com.expensetracker.expenseservice.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.expenseservice.dto.ExpenseRequest;
import com.expensetracker.expenseservice.entity.Expense;
import com.expensetracker.expenseservice.service.ExpenseService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
	
	private final ExpenseService expenseService;
	
	public ExpenseController(ExpenseService expenseService) {
		super();
		this.expenseService = expenseService;
	}
	
	@PostMapping
    public Expense addExpense(@RequestBody ExpenseRequest request, Authentication authentication,HttpServletRequest httpRequest) {
        String username = authentication.getName();
        String email = (String) httpRequest.getAttribute("userEmail");
        return expenseService.addExpense(username, email, request);
    }

    @GetMapping
    public List<Expense> getExpenses(Authentication authentication) {
        String username = authentication.getName();
        return expenseService.getExpensesForUser(username);
    }

	@GetMapping("/hello")
	public String getMessage() {
		return "Hello, Welcome to Smart Expense Tracker Application";
	}
}
