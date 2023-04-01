package com.chatty.core.post;

import com.chatty.core.exception.BadRequestException;
import com.chatty.core.exception.UnauthorizedException;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.zip;

@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostService postService;

    @Autowired
    PostController(final PostRepository postRepository,
                   final UserRepository userRepository,
                   final PostService postService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postService = postService;
    }

    @GetMapping("{id}")
    @JsonView(PostViews.Public.class)
    public Mono<Post> get(@PathVariable final String id) {
        return postService.get(id);
    }

    private Mono<Post> savePost(final Post post) {
        return postService.save(post);
    }

    @PostMapping("/")
    public Mono<Post> create(@AuthenticationPrincipal Mono<UserDetails> principal,
                             @RequestBody Mono<Post> requestPost) {
        return requestPost
                .flatMap(post -> currentUser(principal)
                        .map(appUser -> post.toBuilder().senderId(appUser.getId()).build())
                        .flatMap(this::savePost));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@AuthenticationPrincipal Mono<UserDetails> principal,
                             @RequestBody Mono<Post> requestPost,
                             @PathVariable String id) {

        return requestPost
                .flatMap(updatedPost ->
                        postRepository.findById(id)
                                .flatMap(savedPost -> updatePost(principal, savedPost, updatedPost))
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found"))));
    }

    private Mono<Post> updatePost(Mono<UserDetails> principal, Post savedPost, Post updatePost) {
        String userId = savedPost.getSenderId();
        return currentUser(principal)
                .filter(authUser -> authUser.getId().equals(userId))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Unauthorized Modification")))
                .flatMap(authUser -> postRepository.save(updatePost.toBuilder().build()));
    }

    @GetMapping("/all")
    public Flux<Post> getAll(@AuthenticationPrincipal Mono<UserDetails> principal) {
        return currentUser(principal)
                .flatMapMany(authUser -> postRepository.findAllBySenderId(authUser.getId()));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@AuthenticationPrincipal Mono<UserDetails> principal,
                             @PathVariable String id) {
        return Mono.just(id)
                .flatMap(postId -> postRepository.findById(postId))
                .switchIfEmpty(Mono.error(new BadRequestException("Post not found")))
                .flatMap(savedPost -> deletePost(principal, savedPost));
    }

    private Mono<Void> deletePost(Mono<UserDetails> principal, Post savedPost) {
        String userId = savedPost.getSenderId();
        return currentUser(principal)
                .filter(authUser -> authUser.getId().equals(userId))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Unauthorized Modification")))
                .flatMap(authUser -> postRepository.deleteById(savedPost.getId()));
    }

    private Mono<ApplicationUser> currentUser(Mono<UserDetails> principal) {
        return principal
                .flatMap(authUser -> userRepository.findUserByEmail(authUser.getUsername()));
    }

    @PostMapping("/{id}")
    public Mono<Post> postReply(@AuthenticationPrincipal Mono<UserDetails> principal,
                                @RequestBody Mono<Post> requestPost,
                                @PathVariable String id) {
        Mono<String> parentPostId = Mono.just(id);

        return zip(parentPostId, requestPost, principal).flatMap(tuple3 -> {
            String parentId = tuple3.getT1();
            Post reply = tuple3.getT2();
            UserDetails userDetails = tuple3.getT3();
            return userRepository
                    .findUserByEmail(userDetails.getUsername())
                    .flatMap(user -> saveReply(parentId, reply, user.getId()));
        });
    }

    private Mono<Post> saveReply(String parentPostId,
                                 Post replyPost,
                                 String userId) {
        return postRepository
                .findById(parentPostId)
                .flatMap(parentPost -> {
                    Post reply = replyPost.toBuilder().senderId(userId).parentId(parentPostId).build();
                    return zip(Mono.just(parentPost), Mono.just(reply));
                }).flatMap(tuple -> {
                    if (!tuple.getT1().isRepliesPresent()) {
                        Post parent = tuple.getT1().toBuilder()
                                .repliesPresent(true)
                                .build();
                        return savePost(tuple.getT2()).then(savePost(parent));
                    } else {
                        return savePost(tuple.getT2());
                    }
                });
    }

    @GetMapping("/{id}/replies")
    public Flux<Post> getReplies(@PathVariable String id) {
        return postRepository.findAllByParentId(id);
    }
}

