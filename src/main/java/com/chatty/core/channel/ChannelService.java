package com.chatty.core.channel;

import com.chatty.core.CrudService;
import com.chatty.core.messaging.ChannelPostMessageService;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Set;

@Component
public class ChannelService extends CrudService<Channel, ChannelRepository, String> {
    private final ChannelRepository channelRepository;
    private final UserService userService;
    private final ChannelPostService channelPostService;

    @Autowired
    public ChannelService(ChannelRepository channelRepository,
                          UserService userService,
                          ChannelPostService channelPostService) {
        super(channelRepository);
        this.channelRepository = channelRepository;
        this.userService = userService;
        this.channelPostService = channelPostService;
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
        return get(channelId)
                .flatMap(channel -> {
                    Set<String> userIds = channel.getUserIds();
                    userIds.add(userId);
                    channel.toBuilder()
                            .userIds(userIds)
                            .lastModified(new Date())
                            .build();
                    return save(channel).flatMap((ch) ->
                            userService.getUserElseThrow(userId)
                                    .flatMap(user -> {
                                        Set<String> channelIds = user.getChannelIds();
                                        channelIds.add(ch.getId());
                                        ApplicationUser updatedUser = user.toBuilder()
                                                .spaceIds(channelIds)
                                                .lastModified(new Date())
                                                .build();
                                        return userService.save(updatedUser);
                                    })
                    );
                });
    }
    Mono<ApplicationUser> removeUserFromChannel(String userId, String channelId) {
        return get(channelId)
                .flatMap(channel -> {
                    Set<String> userIds = channel.getUserIds();
                    userIds.remove(userId);
                    Channel updateChannel = channel.toBuilder()
                            .userIds(userIds)
                            .lastModified(new Date())
                            .build();
                    return save(updateChannel).flatMap((ch) ->
                            userService.getUserElseThrow(userId)
                                    .flatMap(user -> {
                                        Set<String> channelIds = user.getChannelIds();
                                        channelIds.remove(ch.getId());
                                        ApplicationUser updatedUser = user.toBuilder()
                                                .spaceIds(channelIds)
                                                .lastModified(new Date())
                                                .build();
                                        return userService.save(updatedUser);
                                    })
                    );
                });
    }
    Mono<ChannelPost> addUserPost(ApplicationUser user, Post post, String channelId) {
        ChannelPost channelPost = ChannelPost.builder()
                .parentId(user.getId())
                .channelId(channelId)
                .content(post.getContent())
                .senderId(user.getId())
                .build();
        return channelPostService.save(channelPost);
    }

    Mono<ChannelPost> addUserPostReply(ApplicationUser user,
                                       String parentPostId,
                                       Post replyPost,
                                       String channelId) {
        return Mono.empty();
    }
}
