package com.chatty.core.messaging;

import com.chatty.core.CrudService;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Date;


@Component
public class EventService extends CrudService<ChannelPostEvent, EventRepository, String> {
    EventRepository eventRepository;
    public EventService(EventRepository eventRepository, ReactiveMongoOperations reactiveMongoOperations) {
        super(eventRepository);
        this.eventRepository = eventRepository;
    }

    public Flux<ChannelPostEvent> eventsAfter(Topic topic, String eventId) {
        return get(eventId).flatMapMany(savedEvent -> {
            Date date = savedEvent.getDateTime();
            return eventRepository.findByDateTimeAfterAndEvent(date, topic.toString()).log();
        });
    }
}
