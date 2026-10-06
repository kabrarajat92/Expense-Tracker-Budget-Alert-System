package com.expensetracker.expenseservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {
	private String category;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LocalDate getExpenseDate() {
		return expenseDate;
	}
	public void setExpenseDate(LocalDate expenseDate) {
		this.expenseDate = expenseDate;
	}
    
    
    
}
