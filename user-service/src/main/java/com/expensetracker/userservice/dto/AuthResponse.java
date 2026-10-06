package com.expensetracker.userservice.dto;

import lombok.Data;

@Data
public class AuthResponse {
	private String token;
    private String username;
    private String role;
    
	public AuthResponse(String token, String username, String role) {
		super();
		this.token = token;
		this.username = username;
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "AuthResponse [token=" + token + ", username=" + username + ", role=" + role + "]";
	}
}
