package com.reactive.notification.kafka.producer;

import com.reactive.notification.service.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;

@Component
@RequiredArgsConstructor
@Slf4j
public class RxKafkaConsumerService {

    private final KafkaReceiver<String, String> kafkaNotificationReceiver;

    private final NotificationService notificationService;

    @PostConstruct
    private void postConstruct() {
        consume();
    }

    public void consume() {
        kafkaNotificationReceiver.receive()
                .map(x -> {

                    String messageValue = x.value();

                    log.info("Consumed :: {}", messageValue);
                    notificationService.consumeMessage(messageValue);

                    x.receiverOffset().acknowledge();

                    return messageValue;

                })
                .subscribe();
    }
}
