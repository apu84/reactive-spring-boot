package com.chatty.api;

import com.chatty.core.user.User;
import com.chatty.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.chatty.api.ResponseUtils.entityURI;
import static com.chatty.api.ResponseUtils.ok;
import static org.springframework.util.Assert.notNull;

@Component
public class UserHandler {
    static final String CONTEXT_URI = "/user/";
    private UserRepository userRepository;

    @Autowired
    UserHandler(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    Mono<ServerResponse> get(final ServerRequest request) {
        final String id = request.pathVariable("id");
        notNull(id, "User id is null");
        return ok().body(userRepository.findById(id), User.class);
    }

    private Mono<User> saveUser(final User user) {
        return userRepository.save(user);
    }
    private Mono<ServerResponse> buildSuccessResponse(final ServerRequest request, final User user) {
        return ServerResponse.created(entityURI(request, user.getId())).build();
    }
    private Mono<ServerResponse> buildErrorResponse(final Throwable e) {
        return ServerResponse.badRequest().body(BodyInserters.fromValue(e));
    }

    private Mono<ServerResponse> buildErrorResponse(final String err) {
        return ServerResponse.badRequest().body(BodyInserters.fromValue(err));
    }

    private Mono<User> findUserByUserName(final User user) {
        return userRepository
                .findUserByUserName(user.getUserName())
                .switchIfEmpty(Mono.just(user));
    }

    Mono<ServerResponse> create(final ServerRequest request) {
        return request.bodyToMono(User.class)
                .flatMap(this::findUserByUserName)
                .filter(usr -> usr.getId() == null)
                .flatMap(usr -> saveUser(usr)
                                    .flatMap(savedUser -> buildSuccessResponse(request, savedUser))
                                    .onErrorResume(this::buildErrorResponse))
                .switchIfEmpty(buildErrorResponse("User name already taken"));
    }

    Mono<ServerResponse> update(final ServerRequest request) {
        final String id = request.pathVariable("id");
        return request.bodyToMono(User.class)
                .flatMap((usr) -> updateUser(id, usr)
                        .flatMap(user -> ServerResponse.noContent().build())
                        .onErrorResume(this::buildErrorResponse));
    }

    private Mono<User> updateUser(final String id, final User inputUser) {
        return userRepository
                .findById(id)
                .flatMap(user -> {
                    if(inputUser.getName() != null) {
                        user.setName(inputUser.getName());
                    }
                    if(inputUser.getAvailability() != null) {
                        user.setAvailability(inputUser.getAvailability());
                    }
                    if(inputUser.getUserStatus() != null) {
                        user.setUserStatus(inputUser.getUserStatus());
                    }
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
    }

    Mono<ServerResponse> getAll(final ServerRequest request) {
        return ok().body(userRepository.findAll(), User.class);
    }

    Mono<ServerResponse> delete(final ServerRequest request) {
        final String id = request.pathVariable("id");
        return userRepository.findById(id)
                .flatMap(user -> userRepository
                        .deleteById(id)
                        .then(ServerResponse.ok().build())
                        .onErrorResume(this::buildErrorResponse)
                )
                .switchIfEmpty(buildErrorResponse(String.format("User with id: %s  not found", id)));
    }
}
