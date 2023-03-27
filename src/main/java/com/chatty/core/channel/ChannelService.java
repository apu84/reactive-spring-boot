package com.chatty.core.channel;

import com.chatty.core.CrudService;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import com.chatty.core.user.ApplicationUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component
public class ChannelService extends CrudService<Channel, ChannelRepository, String> {
    private final ChannelRepository channelRepository;
    public ChannelService(ChannelRepository channelRepository) {
        super(channelRepository);
        this.channelRepository = channelRepository;
    }
    Flux<Channel> getChannelsSubscribedByUser(ApplicationUser user) {
        return Flux.fromIterable(user.getChannelIds())
                .flatMap(channelRepository::findById);
    }
    Mono<Channel> updateChannel(String channelId, Channel updateChannel) {
        return update(channelId, (savedChannel) -> savedChannel.toBuilder()
                .name(updateChannel.getName())
                .label(updateChannel.getLabel())
                .topics(updateChannel.getTopics())
                .lastModified(new Date())
                .build());
    }
    Mono<ApplicationUser> addUserToChannel(String userId, String channelId) {
        return Mono.empty();
    }
    Mono<ApplicationUser> removeUserFromChannel(String userId, String channelId) {
        return Mono.empty();
    }
    Mono<ChannelPost> addUserPost(ApplicationUser user, Post post, String channelId) {
        return Mono.empty();
    }

    Mono<ChannelPost> addUserPostReply(ApplicationUser user,
                                       String parentPostId,
                                       Post replyPost,
                                       String channelId) {
        return Mono.empty();
    }
}
