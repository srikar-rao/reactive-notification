package com.reactive.notification.controller;

import com.reactive.notification.entity.NotificationEntity;
import com.reactive.notification.kafka.producer.RxKafkaProducerService;
import com.reactive.notification.model.NotificationModel;
import com.reactive.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;
    private final RxKafkaProducerService producerService;

    @PostMapping(value = "/publish", produces = MediaType.APPLICATION_JSON_VALUE)
    public NotificationEntity publishNotification(@RequestBody NotificationModel request) {
//        return Flux.range(0, 10)
//                .map(x -> {
//                    NotificationEntity notification = NotificationEntity.builder().userId(request.getUserId()).content(request.getContent()).build();
//                    producerService.sendNotification(notification);
//                    return notification;
//                });
        NotificationEntity notification = NotificationEntity
                .builder()
                .userId(request.getUserId())
                .content(request.getContent())
                .build();

        producerService.sendNotification(notification);
        return notification;
    }

    @GetMapping(value = "/notification/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationEntity> getNotification(@PathVariable String userId) {
        return notificationService.getNotifications(userId);
    }
}
