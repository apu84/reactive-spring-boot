package com.chatty.core.messaging;

import com.chatty.core.post.Post;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EventRepository<T extends Post> extends ReactiveCrudRepository<Event<T>, String> {
}
