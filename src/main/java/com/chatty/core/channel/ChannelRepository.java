package com.chatty.core.channel;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ChannelRepository extends ReactiveCrudRepository<Channel, String> {
}
