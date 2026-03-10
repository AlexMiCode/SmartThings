package com.smartthings.orders.client;

import com.smartthings.common.dto.NotificationCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/api/notifications")
    void create(@RequestBody NotificationCreateRequest request);
}

