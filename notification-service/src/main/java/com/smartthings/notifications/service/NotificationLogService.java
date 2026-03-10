package com.smartthings.notifications.service;

import com.smartthings.common.dto.NotificationCreateRequest;
import com.smartthings.common.dto.NotificationDto;
import com.smartthings.notifications.entity.NotificationEntry;
import com.smartthings.notifications.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class NotificationLogService {
    private static final Logger log = LoggerFactory.getLogger(NotificationLogService.class);

    private final NotificationRepository notificationRepository;

    public NotificationLogService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void create(NotificationCreateRequest request) {
        NotificationEntry entry = new NotificationEntry();
        entry.setOrderId(request.orderId());
        entry.setUserId(request.userId());
        entry.setMessage(request.message());
        NotificationEntry saved = notificationRepository.save(entry);
        log.info("Stored notification id={} for order={}", saved.getId(), saved.getOrderId());
    }

    public List<NotificationDto> findAll() {
        return notificationRepository.findAll().stream()
                .sorted(Comparator.comparing(NotificationEntry::getCreatedAt).reversed())
                .map(entry -> new NotificationDto(
                        entry.getId(),
                        entry.getOrderId(),
                        entry.getUserId(),
                        entry.getMessage(),
                        entry.getCreatedAt()
                ))
                .toList();
    }
}

