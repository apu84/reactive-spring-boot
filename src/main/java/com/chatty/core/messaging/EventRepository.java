package com.chatty.core.messaging;

import com.chatty.core.post.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface EventRepository<T extends Post> extends ReactiveMongoRepository<Event<T>, String> {
}
