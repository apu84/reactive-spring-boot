package com.chatty.core.messaging;

import com.chatty.core.CrudService;
import com.chatty.core.post.Post;
import org.springframework.stereotype.Component;

@Component
public class EventService<T extends Post> extends CrudService<Event<T>, EventRepository<T>, String> {
    public EventService(EventRepository<T> eventRepository) {
        super(eventRepository);
    }
}
