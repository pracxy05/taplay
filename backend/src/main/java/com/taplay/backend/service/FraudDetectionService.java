package com.taplay.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FraudDetectionService {

    @Value("${taplay.fraud-service.url:http://localhost:5000/predict}")
    private String fraudServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isTransactionFraudulent(Long userId, Double amount, String tagId) {
        try {
            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("user_id", userId);
            reqBody.put("amount", amount);
            reqBody.put("hour_of_day", LocalTime.now().getHour());
            reqBody.put("frequency_1h", 1);
            reqBody.put("terminal_id_hash", tagId.hashCode() % 10);

            Map response = restTemplate.postForObject(fraudServiceUrl, reqBody, Map.class);
            if (response != null && response.containsKey("is_fraud")) {
                return (Boolean) response.get("is_fraud");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Fraud detection microservice unreachable. Falling back to default check: " + e.getMessage());
        }
        return false;
    }
}
