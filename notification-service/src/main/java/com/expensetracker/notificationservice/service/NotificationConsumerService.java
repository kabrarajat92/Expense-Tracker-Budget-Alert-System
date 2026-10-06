package com.expensetracker.notificationservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.expensetracker.notificationservice.entity.Notification;
import com.expensetracker.notificationservice.event.BudgetAlertEvent;
import com.expensetracker.notificationservice.repository.NotificationRepository;

@Service
public class NotificationConsumerService {
	
	private final NotificationRepository notificationRepository;
	private final EmailService emailService;

	public NotificationConsumerService(NotificationRepository notificationRepository,EmailService emailService) {
		super();
		this.notificationRepository = notificationRepository;
		this.emailService = emailService;
	}
	
	@KafkaListener(topics = "budget-alert", groupId = "notification-service-group")
	public void consumeBudgetAlery(BudgetAlertEvent event) {
		System.out.println("Received budget alert event: "+event);
		
		Notification notification = new Notification();
        notification.setUsername(event.getUsername());
        notification.setCategory(event.getCategory());
        notification.setBudgetLimit(event.getBudgetLimit());
        notification.setTotalSpent(event.getTotalSpent());
        notification.setPercentageUsed(event.getPercentageUsed());

        notificationRepository.save(notification);
        System.out.println("Notification saved to MongoDB");
        
        try {
            emailService.sendBudgetAlertEmail(event);
            System.out.println("Budget alert email sent to " + event.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send budget alert email: " + e.getMessage());
            // Deliberately not rethrown — a failed email shouldn't cause the Kafka message
            // to be redelivered or block the notification that's already safely in MongoDB
        }
	}
}
