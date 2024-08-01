package com.reactive.notification.service;

import com.reactive.notification.entity.NotificationEntity;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;

public interface NotificationService {
    @SneakyThrows
    void consumeMessage(String message);

    Flux<NotificationEntity> getNotifications(String userId);
}
