package com.chatty.api;

import com.chatty.core.comment.Comment;
import com.chatty.core.comment.CommentRepository;
import com.chatty.core.user.User;
import com.chatty.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.chatty.api.ResponseUtils.entityURI;
import static com.chatty.api.ResponseUtils.ok;
import static org.springframework.util.Assert.notNull;

@Component
public class CommentHandler {
    static final String CONTEXT_URI = "/comment/";
    private CommentRepository commentRepository;

    @Autowired
    CommentHandler(final CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    Mono<ServerResponse> get(final ServerRequest request) {
        final String id = request.pathVariable("id");
        notNull(id, "Comment id is null");
        return ok().body(commentRepository.findById(id), Comment.class);
    }

    private Mono<Comment> saveComment(final Comment comment) {
        return commentRepository.save(comment);
    }
    private Mono<ServerResponse> buildSuccessResponse(final ServerRequest request, final Comment comment) {
        return ServerResponse.created(entityURI(request, comment.getId())).build();
    }
    private Mono<ServerResponse> buildErrorResponse(final Throwable e) {
        return ServerResponse.badRequest().body(BodyInserters.fromValue(e));
    }

    private Mono<ServerResponse> buildErrorResponse(final String err) {
        return ServerResponse.badRequest().body(BodyInserters.fromValue(err));
    }

    Mono<ServerResponse> create(final ServerRequest request) {
        return request.bodyToMono(Comment.class)
                .flatMap(comment -> saveComment(comment)
                        .flatMap(savedComment -> buildSuccessResponse(request, savedComment))
                        .onErrorResume(this::buildErrorResponse));
    }

    Mono<ServerResponse> update(final ServerRequest request) {
        final String id = request.pathVariable("id");
        return request.bodyToMono(Comment.class)
                .flatMap(comment -> updateComment(id, comment)
                        .flatMap(user -> ServerResponse.noContent().build())
                        .onErrorResume(this::buildErrorResponse));
    }

    private Mono<Comment> updateComment(final String id, final Comment inputComment) {
        return commentRepository
                .findById(id)
                .flatMap(comment -> commentRepository.save(inputComment))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Comment not found")));
    }

    Mono<ServerResponse> getAll(final ServerRequest request) {
        return ok().body(commentRepository.findAll(), Comment.class);
    }

    Mono<ServerResponse> delete(final ServerRequest request) {
        final String id = request.pathVariable("id");
        return commentRepository.findById(id)
                .flatMap(user -> commentRepository
                        .deleteById(id)
                        .then(ServerResponse.ok().build())
                        .onErrorResume(this::buildErrorResponse)
                )
                .switchIfEmpty(buildErrorResponse(String.format("Comment with id: %s  not found", id)));
    }
}

