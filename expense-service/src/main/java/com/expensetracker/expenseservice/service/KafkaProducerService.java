package com.expensetracker.expenseservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.expensetracker.expenseservice.event.BudgetAlertEvent;

@Service
public class KafkaProducerService {
	private final KafkaTemplate<String, BudgetAlertEvent> kafkaTemplate;
    private static final String TOPIC = "budget-alert";

    
    public KafkaProducerService(KafkaTemplate<String, BudgetAlertEvent> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}


	public void sendBudgetAlert(BudgetAlertEvent event) {
        kafkaTemplate.send(TOPIC, event.getUsername(), event);
    }
}
