package com.chatty.core.post;

import com.chatty.core.security.UnauthorizedException;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserRepository;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("/post")
public class PostController {
    private PostRepository postRepository;
    private UserRepository userRepository;

    @Autowired
    PostController(final PostRepository postRepository,
                   final UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("{id}")
    @JsonView(PostViews.Public.class)
    public Mono<Post> get(@PathVariable final String id) {
        return postRepository.findById(id).log();
    }

    private Mono<Post> savePost(final Post post) {
        return postRepository.save(post);
    }

    @PostMapping("/")
    public Mono<Post> create(@AuthenticationPrincipal Mono<UserDetails> principal,
                             @RequestBody Mono<Post> requestPost) {
        return requestPost
                .flatMap(post -> currentUser(principal)
                        .map(appUser -> {
                            post.setSenderId(appUser.getId());
                            return post;
                        })
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
                .flatMap(authUser -> {
                    savedPost.setContent(updatePost.getContent());
                    return postRepository.save(savedPost);
                });
    }
    @GetMapping("/all")
    public Flux<Post> getAll(@AuthenticationPrincipal Mono<UserDetails> principal) {
        return currentUser(principal)
                .flatMapMany(authUser -> postRepository.findAllBySenderId(authUser.getId()));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@AuthenticationPrincipal Mono<UserDetails> principal,
                             @PathVariable String id) {

        return postRepository.findById(id)
                .flatMap(savedPost -> deletePost(principal, savedPost))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found")));
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
}

