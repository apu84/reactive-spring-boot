package com.chatty.api;

import com.chatty.core.User;
import com.chatty.core.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;

import static com.chatty.api.ResponseUtils.*;
import static org.springframework.util.Assert.notNull;
import static org.springframework.web.reactive.function.server.ServerResponse.created;

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

    Mono<User> findUserByUserName(final User user) {
        return userRepository
                .findUserByUserName(user.getUserName())
                .switchIfEmpty(Mono.just(user));
    }

    Mono<ServerResponse> create(final ServerRequest request) {
        return request.bodyToMono(User.class)
                .flatMap(this::findUserByUserName)
                .filter(usr -> usr.getId() == null)
                .flatMap(usr -> saveUser(usr).flatMap(savedUser -> ServerResponse.created(entityURI(request, savedUser.getId())).build()).onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BodyInserters.fromValue(e))))
                .switchIfEmpty(ServerResponse.badRequest().body(BodyInserters.fromValue("User name already taken")));
    }

    Mono<ServerResponse> getAll(final ServerRequest request) {
        return ok().body(userRepository.findAll(), User.class);
    }
}
