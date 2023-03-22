package com.chatty.core.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class UserService {
    private final UserRepository userRepository;
    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Mono<ApplicationUser> toApplicationUser(Mono<UserDetails> userDetails) {
        return userDetails.flatMap(user -> userRepository.findUserByEmail(user.getUsername()));
    }
}
