package com.reactive.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.notification.entity.NotificationEntity;
import com.reactive.notification.repository.NotificationRepository;
import com.reactive.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final NotificationRepository notificationRepository;
    private final Map<String, Sinks.Many<NotificationEntity>> userSinks = new ConcurrentHashMap<>();


    @SneakyThrows
    @Override
    public void consumeMessage(String message) {

        NotificationEntity notification = objectMapper.readValue(message, NotificationEntity.class);

        Sinks.Many<NotificationEntity> sink = userSinks.get(notification.getUserId());

        saveMessage(notification)
                .map(savedEntity -> {
                    if (sink != null) {
                        sink.tryEmitNext(notification);
                    }
                    return notification;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private Mono<NotificationEntity> saveMessage(NotificationEntity message) {
        message.setStatus("PENDING");
        return notificationRepository.save(message);
    }

    private void removeUserSink(String userId) {
        userSinks.remove(userId);
    }

    @Override
    public Flux<NotificationEntity> getNotifications(String userId) {

        Sinks.Many<NotificationEntity> sink = userSinks.computeIfAbsent(userId, key -> Sinks.many().multicast().onBackpressureBuffer());

        Flux<NotificationEntity> hotFlux = sink.asFlux();

        Flux<NotificationEntity> coldFlux = notificationRepository.findAllUnNotifiedEvents(userId);

        return coldFlux.mergeWith(hotFlux)
                .doOnNext(entity -> {
                    notificationRepository.findById(entity.getId())
                            .flatMap(x -> {
                                x.setStatus("COMPLETED");
                                return notificationRepository.save(x);
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                })
                .doOnCancel(() -> removeUserSink(userId));
    }
}
