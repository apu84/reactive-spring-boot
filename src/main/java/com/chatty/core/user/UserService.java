package com.chatty.core.user;

import com.chatty.core.CrudService;
import com.chatty.core.exception.BadRequestException;
import com.chatty.core.space.Space;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Set;

@Component
public class UserService extends CrudService<ApplicationUser, UserRepository, String> {
    private final UserRepository userRepository;
    public UserService(final UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }
    public Mono<ApplicationUser> toApplicationUser(Mono<UserDetails> userDetails) {
        return userDetails.flatMap(user -> userRepository.findUserByEmail(user.getUsername()));
    }
    public Mono<ApplicationUser> getUserElseThrow(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new BadRequestException("Invalid userId")));
    }
    public Mono<ApplicationUser> addSpaceToUser(ApplicationUser user, Space space) {
        Set<String> spaces = user.getSpaceIds();
        spaces.add(space.getId());
        ApplicationUser updateUser = user.toBuilder().spaceIds(spaces).lastModified(new Date()).build();
        return userRepository.save(updateUser);
    }
}
