package com.chatty.core.channel;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ChannelRepository extends ReactiveCrudRepository<Channel, String> {
}
