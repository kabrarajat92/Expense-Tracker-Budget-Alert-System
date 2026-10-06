package com.expensetracker.expenseservice.dto;

public class BudgetSuggestionResponse {

	private String suggestions;

	public BudgetSuggestionResponse(String suggestions) {
		super();
		this.suggestions = suggestions;
	}

	public String getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(String suggestions) {
		this.suggestions = suggestions;
	}
	
	
}
