package com.chatty.core.channel;

import com.chatty.core.CrudService;
import com.chatty.core.messaging.ChannelPostMessageService;
import com.chatty.core.post.ChannelPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Mono.zip;

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
                .flatMap(savedPost -> zip(Mono.just(savedPost), channelPostMessageService.sendMessage(savedPost)))
                .map(Tuple2::getT1);
    }
}
