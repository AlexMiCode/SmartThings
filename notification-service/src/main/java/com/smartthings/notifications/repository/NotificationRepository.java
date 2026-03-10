package com.smartthings.notifications.repository;

import com.smartthings.notifications.entity.NotificationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntry, Long> {
}

