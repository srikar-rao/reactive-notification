package com.reactive.notification.kafka.producer;

import com.reactive.notification.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@RequiredArgsConstructor
@Slf4j
@Component
public class RxKafkaProducerService {

    private final KafkaSender<String, Object> kafkaSender;


    public void sendNotification(NotificationEntity notification) {
        kafkaSender.send(
                Mono.just(
                        SenderRecord
                                .create(
                                        "reactive-notification",
                                        null,
                                        null,
                                        notification.getUserId().toString(),
                                        notification,
                                        null)
                )
        ).subscribe();
    }
}
