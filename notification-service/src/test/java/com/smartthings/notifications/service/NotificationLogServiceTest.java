package com.smartthings.notifications.service;

import com.smartthings.common.dto.NotificationCreateRequest;
import com.smartthings.notifications.entity.NotificationEntry;
import com.smartthings.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationLogServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationLogService notificationLogService;

    @Test
    void createPersistsNotificationEntry() {
        when(notificationRepository.save(any(NotificationEntry.class))).thenAnswer(invocation -> {
            NotificationEntry entry = invocation.getArgument(0);
            setMeta(entry, 1L, Instant.now());
            return entry;
        });

        notificationLogService.create(new NotificationCreateRequest(10L, 3L, "Order created"));

        org.mockito.Mockito.verify(notificationRepository).save(any(NotificationEntry.class));
    }

    @Test
    void findAllReturnsEntriesSortedDescending() {
        NotificationEntry older = entry(1L, "older", Instant.now().minusSeconds(30));
        NotificationEntry newer = entry(2L, "newer", Instant.now());
        when(notificationRepository.findAll()).thenReturn(List.of(older, newer));

        var result = notificationLogService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().message()).isEqualTo("newer");
    }

    @Test
    void findAllMapsOrderAndUserIds() {
        when(notificationRepository.findAll()).thenReturn(List.of(entry(5L, "mapped", Instant.now())));

        var result = notificationLogService.findAll();

        assertThat(result.getFirst().id()).isEqualTo(5L);
        assertThat(result.getFirst().message()).isEqualTo("mapped");
    }

    private NotificationEntry entry(Long id, String message, Instant createdAt) {
        NotificationEntry entry = new NotificationEntry();
        entry.setOrderId(10L);
        entry.setUserId(3L);
        entry.setMessage(message);
        setMeta(entry, id, createdAt);
        return entry;
    }

    private void setMeta(NotificationEntry entry, Long id, Instant createdAt) {
        try {
            var idField = NotificationEntry.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entry, id);
            var createdAtField = NotificationEntry.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entry, createdAt);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}

