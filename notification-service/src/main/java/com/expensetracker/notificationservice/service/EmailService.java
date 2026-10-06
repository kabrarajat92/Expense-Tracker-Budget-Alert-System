package com.expensetracker.notificationservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.expensetracker.notificationservice.event.BudgetAlertEvent;

@Service
public class EmailService {
	
	private final JavaMailSender mailSender;

	public EmailService(JavaMailSender mailSender) {
		super();
		this.mailSender = mailSender;
	}
	
	public void sendBudgetAlertEmail(BudgetAlertEvent event) {
        if (event.getEmail() == null || event.getEmail().isBlank()) {
            System.out.println("No email available for user " + event.getUsername() + ", skipping email alert.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getEmail());
        message.setSubject("Budget Alert: " + event.getCategory() + " is at "
                + String.format("%.0f", event.getPercentageUsed()) + "%");
        message.setText(
                "Hi " + event.getUsername() + ",\n\n" +
                "You've used " + String.format("%.0f", event.getPercentageUsed()) +
                "% of your ₹" + event.getBudgetLimit() + " budget for " + event.getCategory() + ".\n" +
                "Total spent so far: ₹" + event.getTotalSpent() + "\n\n" +
                "— Smart Expense Tracker"
        );

        mailSender.send(message);
    }
}
