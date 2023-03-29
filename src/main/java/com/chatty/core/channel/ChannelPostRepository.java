package com.chatty.core.channel;

import com.chatty.core.post.ChannelPost;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ChannelPostRepository extends ReactiveCrudRepository<ChannelPost, String> {
}
