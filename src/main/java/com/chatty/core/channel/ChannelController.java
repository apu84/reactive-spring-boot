package com.chatty.core.channel;

import com.chatty.core.messaging.ChannelPostMessageService;
import com.chatty.core.messaging.Event;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.zip;

@RequestMapping("/channel")
@RestController
public class ChannelController {
    private final ChannelService channelService;
    private final UserService userService;
    private final ChannelPostService channelPostService;
    private final ChannelPostMessageService channelPostMessageService;
    @Autowired
    public ChannelController(final ChannelService channelService,
                             final UserService userService,
                             final ChannelPostService channelPostService,
                             final ChannelPostMessageService channelPostMessageService) {
        this.channelService = channelService;
        this.userService = userService;
        this.channelPostService = channelPostService;
        this.channelPostMessageService = channelPostMessageService;
    }
    @GetMapping("/{id}")
    public Mono<Channel> getChannel(@PathVariable String id) {
        return channelService.get(id);
    }
    @GetMapping
    public Flux<Channel> getAllChannels() {
        return channelService.getAll();
    }

    @GetMapping("/subscribed")
    public Flux<Channel> getAllChannelsForCurrentUser(@AuthenticationPrincipal Mono<UserDetails> principal) {
        return userService.toApplicationUser(principal)
                .flatMapMany(channelService::getChannelsSubscribedByUser);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Channel> updateChannel(@PathVariable String id,
                                       @RequestBody Mono<Channel> updateChannel) {
        return updateChannel.flatMap(ch -> channelService.updateChannel(id, ch));
    }
    @DeleteMapping("/{id}")
    public Mono<Void> deleteChannel(@PathVariable String id) {
        return channelService.delete(id);
    }
    @PostMapping("/{channelId}/user")
    public Mono<ApplicationUser> addUserToChannel(@RequestBody Mono<ApplicationUser> applicationUserMono,
                                       @PathVariable String channelId) {
        return applicationUserMono.flatMap(user -> channelService.addUserToChannel(user.getId(), channelId));
    }
    @PostMapping("/{channelId}/add-currentUser")
    public Mono<ApplicationUser> addCurrentUserToChannel(@AuthenticationPrincipal Mono<UserDetails> principal,
                                       @PathVariable String channelId) {
        return userService.toApplicationUser(principal)
                .flatMap(user -> channelService.addUserToChannel(user.getId(), channelId));
    }
    @PostMapping("/{channelId}/remove-currentUser")
    public Mono<ApplicationUser> removeCurrentUserFromChannel(@AuthenticationPrincipal Mono<UserDetails> principal,
                                              @PathVariable String channelId) {
        return userService.toApplicationUser(principal)
                .flatMap(user -> channelService.removeUserFromChannel(user.getId(), channelId));
    }
    @PostMapping("/{channelId}/remove-user")
    public Mono<ApplicationUser> removeUserFromChannel(@RequestBody Mono<ApplicationUser> applicationUserMono,
                                            @PathVariable String channelId) {
        return applicationUserMono.flatMap(user -> channelService.removeUserFromChannel(user.getId(), channelId));
    }

    @PostMapping("/{channelId}/post")
    public Mono<ChannelPost> addUserPost(@AuthenticationPrincipal Mono<UserDetails> principal,
                                         @RequestBody Mono<Post> postMono,
                                         @PathVariable String channelId) {
        return zip(userService.toApplicationUser(principal), postMono)
                .flatMap(tuple -> channelPostService.addUserPost(tuple.getT1(), tuple.getT2(), channelId));
    }

    @PostMapping("/{channelId}/post/{parentPostId}/replies")
    public Mono<ChannelPost> addReply(@AuthenticationPrincipal Mono<UserDetails> principal,
                                      @RequestBody Mono<Post> postMono,
                                      @PathVariable String channelId,
                                      @PathVariable String parentPostId) {
        return zip(userService.toApplicationUser(principal), postMono)
                .flatMap(tuple -> channelService.addUserPostReply(tuple.getT1(), parentPostId, tuple.getT2(), channelId));
    }

    @GetMapping(value = "/{channelId}/post/subscribe", produces = "text/event-stream;charset=UTF-8")
    public Flux<Event<ChannelPost>> subscribeToChannelPost(@PathVariable String channelId) {
        return channelPostService
                .buildTopic(channelId)
                .flatMapMany(channelPostMessageService::consumeMessage);
    }
}
