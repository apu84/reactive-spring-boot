package com.chatty.core.channel;

import com.chatty.core.CrudService;
import com.chatty.core.messaging.ChannelPostMessageService;
import com.chatty.core.messaging.Topic;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import com.chatty.core.space.SpaceService;
import com.chatty.core.user.ApplicationUser;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Mono.zip;

@Component
public class ChannelPostService extends CrudService<ChannelPost, ChannelPostRepository, String> {
    private final ChannelPostMessageService channelPostMessageService;
    private final ChannelService channelService;
    private final SpaceService spaceService;

    @Autowired
    public ChannelPostService(ChannelPostRepository repository,
                              ChannelPostMessageService channelPostMessageService,
                              ChannelService channelService,
                              SpaceService spaceService) {
        super(repository);
        this.channelPostMessageService = channelPostMessageService;
        this.spaceService = spaceService;
        this.channelService = channelService;
    }

    Mono<ChannelPost> addUserPost(@NonNull ApplicationUser user, @NonNull Post post, String channelId) {
        ChannelPost channelPost = ChannelPost.builder()
                .parentId(user.getId())
                .channelId(channelId)
                .content(post.getContent())
                .senderId(user.getId())
                .build();
        return save(channelPost);
    }

    @Override
    public Mono<ChannelPost> save(ChannelPost channelPost) {
        return super.save(channelPost)
                .flatMap(savedPost -> zip(Mono.just(savedPost),
                        channelPostMessageService
                                .sendMessage(buildTopic(channelPost), savedPost)))
                .map(Tuple2::getT1);
    }

    public Mono<Topic> buildTopic(@NonNull ChannelPost channelPost) {
        return buildTopic(channelPost.getChannelId());
    }

    public Mono<Topic> buildTopic(@NonNull String channelId) {
        return channelService
                .get(channelId)
                .flatMap(channel -> zip(just(channel), spaceService.get(channel.getSpaceId())))
                .map(tuple -> Topic.builder()
                        .spaceName(tuple.getT2().getName())
                        .channelName(tuple.getT1().getName())
                        .build());
    }
}
