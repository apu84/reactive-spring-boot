package com.chatty.core.space;

import com.chatty.core.exception.BadRequestException;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;

import static reactor.core.publisher.Mono.zip;

@Component
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final UserService userService;

    @Autowired
    SpaceService(SpaceRepository spaceRepository,
                 UserService userService) {
        this.spaceRepository = spaceRepository;
        this.userService = userService;
    }
    Mono<Space> saveSpace(Space space) {
        return spaceRepository.save(space);
    }

    Mono<Space> updateSpace(String spaceId, Space updateSpace) {
        return getSpaceElseThrow(spaceId)
                .flatMap(space -> spaceRepository.save(updateSpace(space, updateSpace)));
    }

    Mono<ApplicationUser> addUserToSpace(String userId, String spaceId) {
        return zip(getSpaceElseThrow(spaceId), userService.getUserElseThrow(userId))
                .flatMap(tuple -> userService.addSpaceToUser(tuple.getT2(), tuple.getT1()));
    }

    public Mono<Space> getSpaceElseThrow(String spaceId) {
        return spaceRepository.findById(spaceId)
                .switchIfEmpty(Mono.error(new BadRequestException("Invalid space")));
    }

    static Space updateSpace(Space oldSpace, Space newSpace) {
        return oldSpace.toBuilder()
                .name(newSpace.getName())
                .label(newSpace.getLabel())
                .lastModified(new Date())
                .build();
    }
}
