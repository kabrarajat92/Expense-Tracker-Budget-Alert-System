package com.expensetracker.notificationservice.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.expensetracker.notificationservice.entity.Notification;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
	List<Notification> findByUsername(String username);
    List<Notification> findByUsernameAndReadFalse(String username);
}
