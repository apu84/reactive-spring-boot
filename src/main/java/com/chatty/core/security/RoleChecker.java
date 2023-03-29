package com.chatty.core.security;

import com.chatty.core.space.SpaceRepository;
import com.chatty.core.user.UserRepository;
import com.chatty.core.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@Slf4j
public class RoleChecker {
    private final SpaceRepository spaceRepository;
    private final UserService userService;
    @Autowired
    public RoleChecker(SpaceRepository spaceRepository,
                       UserService userService) {
        this.spaceRepository = spaceRepository;
        this.userService = userService;
    }
    public Mono<Boolean> isSpaceAdmin(String spaceId, Mono<UserDetails> userDetailsMono) {
        log.debug("space id: " + spaceId);
        return userService
                .toApplicationUser(userDetailsMono)
                .map(Objects::nonNull);
    }
}
