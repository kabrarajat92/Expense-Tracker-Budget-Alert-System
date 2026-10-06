package com.expensetracker.expenseservice.dto;

import java.math.BigDecimal;

public class BudgetRequest {
	private String category;
    private BigDecimal limitAmount;
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public BigDecimal getLimitAmount() {
		return limitAmount;
	}
	public void setLimitAmount(BigDecimal limitAmount) {
		this.limitAmount = limitAmount;
	}
    
}
