package com.expensetracker.expenseservice.dto;

public class ForecastResponse {
	
	private String forecast;

	public ForecastResponse(String forecast) {
		super();
		this.forecast = forecast;
	}

	public String getForecast() {
		return forecast;
	}

	public void setForecast(String forecast) {
		this.forecast = forecast;
	}
	
}
