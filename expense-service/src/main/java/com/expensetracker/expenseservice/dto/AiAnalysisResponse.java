package com.expensetracker.expenseservice.dto;

public class AiAnalysisResponse {
	
	private String analysis;
	
	public AiAnalysisResponse(String analysis) {
		super();
		this.analysis = analysis;
	}

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}
	
	
}
