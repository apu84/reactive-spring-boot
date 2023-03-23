package com.chatty.core.channel;

import com.chatty.core.exception.BadRequestException;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import com.chatty.core.user.ApplicationUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static com.chatty.core.channel.Channel.fromChannel;

@Component
public class ChannelService {
    private final ChannelRepository channelRepository;
    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }
    Mono<Channel> saveChannel(Channel channel) {
        return channelRepository.save(channel);
    }
    Mono<Channel> getChannel(String channelId) {
        return channelRepository.findById(channelId);
    }
    Flux<Channel> getAllChannels() {
        return channelRepository.findAll();
    }
    Flux<Channel> getChannelsSubscribedByUser(ApplicationUser user) {
        return Flux.fromIterable(user.getChannelIds())
                .flatMap(channelRepository::findById);
    }
    Mono<Channel> updateChannel(String channelId, Channel updateChannel) {
        return Mono.just(channelId)
                .flatMap(chId -> channelRepository.findById(channelId))
                .switchIfEmpty(Mono.error(new BadRequestException("Invalid channelId")))
                .flatMap(channel -> {
                    Channel updated = fromChannel(channel)
                            .name(updateChannel.getName())
                            .label(updateChannel.getLabel())
                            .topics(updateChannel.getTopics())
                            .build();
                    return channelRepository.save(updated);
                });
    }
    Mono<Void> deleteChannel(String channelId) {
        return Mono.just(channelId)
                .flatMap(chId -> channelRepository.findById(chId))
                .switchIfEmpty(Mono.error(new BadRequestException("Invalid channelId")))
                .flatMap(channelRepository::delete);
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
