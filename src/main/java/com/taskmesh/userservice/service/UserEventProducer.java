package com.taskmesh.userservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserCreated(Long userId) {
        kafkaTemplate.send("USER_CREATED", userId.toString());
    }

    public void publishUserDeactivated(Long userId) {
        kafkaTemplate.send("USER_DEACTIVATED", userId.toString());
    }
}
