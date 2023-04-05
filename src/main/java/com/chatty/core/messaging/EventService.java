package com.chatty.core.messaging;

import com.chatty.core.CrudService;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.lang.reflect.ParameterizedType;
import java.util.Date;

import static org.springframework.data.mongodb.core.query.Criteria.where;


@Component
public class EventService<T extends Post> extends CrudService<Event<T>, EventRepository<T>, String> {
    private final ReactiveMongoOperations reactiveMongoOperations;
    public EventService(EventRepository<T> eventRepository, ReactiveMongoOperations reactiveMongoOperations) {
        super(eventRepository);
        this.reactiveMongoOperations = reactiveMongoOperations;
    }

    public Flux<Event<ChannelPost>> eventsAfter(String eventId) {
        return get(eventId)
                .flatMapMany(savedEvent -> {
            Date date = savedEvent.getDateTime();
            return this.reactiveMongoOperations.find(
                    new Query(where("datetime").gt(date)),
                    Event.class);
        });
    }
}
