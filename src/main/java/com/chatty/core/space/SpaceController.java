package com.chatty.core.space;

import com.chatty.core.channel.Channel;
import com.chatty.core.channel.ChannelService;
import com.chatty.core.user.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Date;

@RestController
@RequestMapping("/space")
public class SpaceController {
    private final SpaceService spaceService;
    private final ChannelService channelService;
    @Autowired
    public SpaceController(SpaceService spaceService,
                           ChannelService channelService) {
      this.spaceService = spaceService;
      this.channelService = channelService;
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Space> createSpace(@RequestBody Mono<Space> space) {
        return space.flatMap(spaceService::save);
    }
    @PutMapping("/{id}")
    public Mono<Space> updateSpace(@PathVariable String id,
                                   @RequestBody Mono<Space> space) {
        return space.flatMap(updateSpace -> spaceService.updateSpace(id, updateSpace));
    }
    @PostMapping("/{id}/user")
    public Mono<ApplicationUser> addUserToSpace(@PathVariable String id,
                                                @RequestBody Mono<String> userId) {
        return userId.flatMap(usrId -> spaceService.addUserToSpace(usrId, id));
    }

    @PostMapping("/{id}/channel")
    @PreAuthorize("hasRole('SPACE_ADMIN')")
    public Mono<Channel> addChannelToSpace(@PathVariable String id,
                                           @RequestBody Mono<Channel> channelMono) {
        return channelMono
                .map(channel -> channel.toBuilder().spaceId(id).lastModified(new Date()).build())
                .flatMap(channelService::save);
    }
}
