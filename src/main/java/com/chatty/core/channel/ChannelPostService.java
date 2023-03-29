package com.chatty.core.channel;

import com.chatty.core.CrudService;
import com.chatty.core.messaging.ChannelPostMessageService;
import com.chatty.core.post.ChannelPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ChannelPostService extends CrudService<ChannelPost, ChannelPostRepository, String> {
    private final ChannelPostMessageService channelPostMessageService;
    @Autowired
    public ChannelPostService(ChannelPostRepository repository,
                              ChannelPostMessageService channelPostMessageService) {
        super(repository);
        this.channelPostMessageService = channelPostMessageService;
    }

    @Override
    public Mono<ChannelPost> save(ChannelPost channelPost) {
        return super.save(channelPost)
                .flatMap(savedPost -> Mono.fromFuture(channelPostMessageService.sendMessage(savedPost))
                        .thenReturn(savedPost));
    }
}
