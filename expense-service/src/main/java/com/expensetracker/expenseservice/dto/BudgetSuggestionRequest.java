package com.expensetracker.expenseservice.dto;

import java.math.BigDecimal;

public class BudgetSuggestionRequest {

	private BigDecimal monthlyIncome;

	public BigDecimal getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(BigDecimal monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}
	
}
