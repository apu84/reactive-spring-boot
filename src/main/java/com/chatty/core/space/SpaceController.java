package com.chatty.core.space;

import com.chatty.core.user.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/space")
public class SpaceController {
    private final SpaceService spaceService;
    @Autowired
    public SpaceController(SpaceService spaceService) {
      this.spaceService = spaceService;
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Space> createSpace(@RequestBody Mono<Space> space) {
        return space.flatMap(spaceService::saveSpace);
    }
    @PutMapping("/{id}")
    public Mono<Space> updateSpace(@PathVariable String id,
                                   @RequestBody Mono<Space> space) {
        return space.flatMap(updateSpace -> spaceService.updateSpace(id, updateSpace));
    }
    @PostMapping("/{id}")
    public Mono<ApplicationUser> addUserToSpace(@PathVariable String id,
                                                @RequestBody Mono<String> userId) {
        return userId.flatMap(usrId -> spaceService.addUserToSpace(usrId, id));
    }
}
