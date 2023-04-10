package com.chatty.core.messaging;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.Date;

public interface EventRepository extends ReactiveMongoRepository<ChannelPostEvent, String> {
    Flux<ChannelPostEvent> findByDateTimeAfter(Date datetime);
}
