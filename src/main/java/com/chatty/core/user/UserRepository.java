package com.chatty.core.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<ApplicationUser, String> {
    Mono<ApplicationUser> findUserByEmail(String email);
}
