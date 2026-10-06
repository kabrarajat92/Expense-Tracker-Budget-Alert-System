package com.expensetracker.notificationservice.controller;

import java.util.List;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.notificationservice.entity.Notification;
import com.expensetracker.notificationservice.repository.NotificationRepository;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
	
	private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
		super();
		this.notificationRepository = notificationRepository;
	}

	@GetMapping
    public List<Notification> getAll(Authentication authentication) {
        return notificationRepository.findByUsername(authentication.getName());
    }

    @GetMapping("/unread")
    public List<Notification> getUnread(Authentication authentication) {
        return notificationRepository.findByUsernameAndReadFalse(authentication.getName());
    }

    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

}
