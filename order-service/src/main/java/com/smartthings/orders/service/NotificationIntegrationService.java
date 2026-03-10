package com.smartthings.orders.service;

import com.smartthings.common.dto.NotificationCreateRequest;
import com.smartthings.orders.client.NotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationIntegrationService.class);

    private final NotificationClient notificationClient;

    public NotificationIntegrationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void sendOrderCreated(Long orderId, Long userId, String customerName) {
        try {
            notificationClient.create(new NotificationCreateRequest(
                    orderId,
                    userId,
                    "Создан новый заказ #" + orderId + " для клиента " + customerName
            ));
        } catch (Exception ex) {
            log.warn("Notification service is unavailable for order {}", orderId, ex);
        }
    }
}

