package com.reactive.notification.repository;

import com.reactive.notification.entity.NotificationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRepository extends ReactiveCrudRepository<NotificationEntity, Long> {

    @Query("SELECT * FROM notification where user_id = $1 and status <> 'COMPLETED'")
    Flux<NotificationEntity> findAllUnNotifiedEvents(String userId);
}
