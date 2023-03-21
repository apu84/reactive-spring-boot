package com.chatty.core.post;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PostRepository extends ReactiveCrudRepository<Post, String> {
    Flux<Post> findAllBySenderId(String senderId);

    Flux<Post> findAllByParentId(String parentId);
}
