package com.smartthings.notifications.controller;

import com.smartthings.common.dto.NotificationCreateRequest;
import com.smartthings.common.dto.NotificationDto;
import com.smartthings.notifications.service.NotificationLogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationLogService notificationLogService;

    public NotificationController(NotificationLogService notificationLogService) {
        this.notificationLogService = notificationLogService;
    }

    @GetMapping
    public List<NotificationDto> findAll() {
        return notificationLogService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody NotificationCreateRequest request) {
        notificationLogService.create(request);
    }
}

