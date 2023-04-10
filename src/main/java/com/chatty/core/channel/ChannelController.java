package com.chatty.core.channel;

import com.chatty.core.messaging.ChannelPostEvent;
import com.chatty.core.messaging.ChannelPostMessageService;
import com.chatty.core.messaging.Event;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserService;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import static reactor.core.publisher.Mono.zip;

@RequestMapping("/channel")
@RestController
@Slf4j
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
    public Flux<ChannelPostEvent> subscribeToChannelPost(
            @RequestHeader(name="last-event-id", required = false) String lastEventId,
            @PathVariable String channelId) {
        return keepAlive(Duration.ofSeconds(10), subscribeTo(channelId, lastEventId), channelId);
    }

    private Flux<ChannelPostEvent> subscribeTo(String channelId, String lastEventId) {
        return channelPostService
                .buildTopic(channelId)
                .flatMapMany(topic -> channelPostMessageService.consumeMessage(topic, lastEventId));
    }

    private Flux<ChannelPostEvent> keepAlive(Duration duration, Flux<ChannelPostEvent> data, String id) {
        Flux<ChannelPostEvent> heartBeat = Flux.interval(duration)
                .map(e -> getHearBeatObject(id))
                .doFinally(signalType -> log.info("Heartbeat closed for id: {}", id));
        return Flux.merge(heartBeat, data);
    }

    private ChannelPostEvent getHearBeatObject(String heartBeatFor) {
        return ChannelPostEvent.builder()
                .id(UUID.randomUUID().toString())
                .event("Keep alive for: " + heartBeatFor)
                .eventType(Event.EventType.KEEP_ALIVE)
                .dateTime(new Date())
                .build();
    }
}
