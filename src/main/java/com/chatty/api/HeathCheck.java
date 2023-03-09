package com.chatty.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/health-check")
public class HeathCheck {
    private final Mono<SecurityContext> context  = ReactiveSecurityContextHolder.getContext();
    @Autowired
    public HeathCheck() {}

    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("Pong");
    }
    @GetMapping("/current-user")
    public Mono<Map<String, Object>> currentUser(@AuthenticationPrincipal Mono<UserDetails> principal) {
        return principal.map(user -> Map.of(
                "userName", user.getUsername(),
                "roles", AuthorityUtils.authorityListToSet(user.getAuthorities())));
    }
}
