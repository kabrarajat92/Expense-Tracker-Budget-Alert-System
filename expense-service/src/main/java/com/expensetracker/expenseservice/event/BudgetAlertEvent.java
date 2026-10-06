package com.expensetracker.expenseservice.event;

import java.math.BigDecimal;

public class BudgetAlertEvent {
	private String username;
	private String email;
	private String category;
    private BigDecimal budgetLimit;
    private BigDecimal totalSpent;
    private double percentageUsed;
    
    public BudgetAlertEvent(String username, String email, String category, BigDecimal budgetLimit,
			BigDecimal totalSpent, double percentageUsed) {
		super();
		this.username = username;
		this.email = email;
		this.category = category;
		this.budgetLimit = budgetLimit;
		this.totalSpent = totalSpent;
		this.percentageUsed = percentageUsed;
	}
     
	public BudgetAlertEvent() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public BigDecimal getBudgetLimit() {
		return budgetLimit;
	}
	public void setBudgetLimit(BigDecimal budgetLimit) {
		this.budgetLimit = budgetLimit;
	}
	public BigDecimal getTotalSpent() {
		return totalSpent;
	}
	public void setTotalSpent(BigDecimal totalSpent) {
		this.totalSpent = totalSpent;
	}
	public double getPercentageUsed() {
		return percentageUsed;
	}
	public void setPercentageUsed(double percentageUsed) {
		this.percentageUsed = percentageUsed;
	}
    
    
}
