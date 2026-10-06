package com.expensetracker.notificationservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="alerts")
public class Notification {
	
	@Id
    private String id;
    private String username;
    private String category;
    private BigDecimal budgetLimit;
    private BigDecimal totalSpent;
    private double percentageUsed;
    private boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
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
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
    
    
}
