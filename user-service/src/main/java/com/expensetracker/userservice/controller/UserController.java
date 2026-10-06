package com.expensetracker.userservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	@GetMapping("/profile")
    public String profile(Authentication authentication) {
		System.out.println("Authentication object: " + authentication);
        return "Hello, " + authentication.getName() + "! You are authenticated.";
    }
	
	@GetMapping("/hello")
	public String getMessage() {
		return "Hello, Welcome to Smart Expense Tracker Application";
	}
}
