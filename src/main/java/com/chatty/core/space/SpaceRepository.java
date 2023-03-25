package com.chatty.core.space;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SpaceRepository extends ReactiveCrudRepository<Space, String> {
}
