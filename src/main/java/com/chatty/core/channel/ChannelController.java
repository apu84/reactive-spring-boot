package com.chatty.core.channel;

import com.chatty.core.post.ChannelPost;
import com.chatty.core.post.Post;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    public ChannelController(final ChannelService channelService,
                             final UserService userService) {
        this.channelService = channelService;
        this.userService = userService;
    }
    @PostMapping
    public Mono<Channel> createChannel(@RequestBody Mono<Channel> channelMono) {
        return channelMono.flatMap(channelService::saveChannel);
    }
    @GetMapping("/{id}")
    public Mono<Channel> getChannel(@PathVariable String id) {
        return channelService.getChannel(id);
    }
    @GetMapping
    public Flux<Channel> getAllChannels() {
        return channelService.getAllChannels();
    }

    @GetMapping("/subscribed")
    public Flux<Channel> getAllChannelsForCurrentUser(@AuthenticationPrincipal Mono<UserDetails> principal) {
        return userService.toApplicationUser(principal)
                .flatMapMany(channelService::getChannelsSubscribedByUser);
    }
    @PutMapping("/{id}")
    public Mono<Channel> updateChannel(@PathVariable String id,
                                       @RequestBody Mono<Channel> updateChannel) {
        return updateChannel.flatMap(ch -> channelService.updateChannel(id, ch));
    }
    @DeleteMapping("/{id}")
    public Mono<Void> deleteChannel(@PathVariable String id) {
        return channelService.deleteChannel(id);
    }
    @PostMapping("/{channelId}/user")
    public Mono<Void> addUserToChannel(@RequestBody Mono<ApplicationUser> applicationUserMono,
                                       @PathVariable String channelId) {
        return applicationUserMono.flatMap(user -> channelService.addUserToChannel(user.getId(), channelId));
    }
    @PostMapping("/{channelId}/subscribe")
    public Mono<Void> addCurrentUserToChannel(@AuthenticationPrincipal Mono<UserDetails> principal,
                                       @PathVariable String channelId) {
        return userService.toApplicationUser(principal)
                .flatMap(user -> channelService.addUserToChannel(user.getId(), channelId));
    }
    @PostMapping("/{channelId}/unsubscribe")
    public Mono<Void> removeCurrentUserFromChannel(@AuthenticationPrincipal Mono<UserDetails> principal,
                                              @PathVariable String channelId) {
        return userService.toApplicationUser(principal)
                .flatMap(user -> channelService.removeUserFromChannel(user.getId(), channelId));
    }
    @PostMapping("/{channelId}/remove-user")
    public Mono<Void> removeUserFromChannel(@RequestBody Mono<ApplicationUser> applicationUserMono,
                                            @PathVariable String channelId) {
        return applicationUserMono.flatMap(user -> channelService.removeUserFromChannel(user.getId(), channelId));
    }

    @PostMapping("/{channelId}/post")
    public Mono<ChannelPost> addUserPost(@AuthenticationPrincipal Mono<UserDetails> principal,
                                         @RequestBody Mono<Post> postMono,
                                         @PathVariable String channelId) {
        return zip(userService.toApplicationUser(principal), postMono)
                .flatMap(tuple -> channelService.addUserPost(tuple.getT1(), tuple.getT2(), channelId));
    }

    @PostMapping("/{channelId}/post/{parentPostId}/replies")
    public Mono<ChannelPost> addReply(@AuthenticationPrincipal Mono<UserDetails> principal,
                                      @RequestBody Mono<Post> postMono,
                                      @PathVariable String channelId,
                                      @PathVariable String parentPostId) {
        return zip(userService.toApplicationUser(principal), postMono)
                .flatMap(tuple -> channelService.addUserPostReply(tuple.getT1(), parentPostId, tuple.getT2(), channelId));
    }
}
