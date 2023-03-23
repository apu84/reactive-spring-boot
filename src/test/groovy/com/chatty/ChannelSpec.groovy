package com.chatty

import com.chatty.core.channel.Channel
import com.chatty.core.channel.ChannelController
import com.chatty.core.channel.ChannelRepository
import com.chatty.core.post.ChannelPost
import com.chatty.core.post.PostRepository
import com.chatty.core.security.AuthWebClient
import com.chatty.core.user.ApplicationUser
import com.chatty.core.user.UserRepository
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import reactor.core.publisher.Mono

@WebFluxTest(controllers= ChannelController.class)
class ChannelSpec extends BaseSpecification {
    @SpringBean
    AuthWebClient authWebClient = Stub()

    @Autowired
    ChannelRepository  channelRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    PostRepository postRepository

    String token = "Bearer token"

    def setup() {
    }

    def setupLoggedInUser(String name, String email, String role) {
        var loggedInUser = new ApplicationUser(name, email, List.of("ROLE_" + role))
        authWebClient.validate(token) >> Mono.just(loggedInUser)
    }

    def setupLoggedInUser(String role) {
        setupLoggedInUser("test", "test@test.com", role)
    }

    def setupLoggedInUserAsAdmin() {
        setupLoggedInUser("ADMIN")
    }

    def "Posting Channel object persists channel"() {
        given: "A Channel object with name=gateway, label=API Gateway and topic=api,gateway"
            setupLoggedInUserAsAdmin()
            var channel = """{
                        "name": "gateway",
                        "label": "API Gateway",
                        "topics": "api,gateway"
                    }"""

        when: "Channel is posted to /channel endpoint"
            var response = webTestClient.post()
            .uri('/channel')
            .header("Content-Type", "application/json")
            .header('Authorization', token)
            .body(Mono.just(channel), String.class)
            .exchange()
            .returnResult(Map.class)

        then: "Returned Object will contained Channel with unique id, name=gateway"
            Map<String, String> body = response.responseBody.blockFirst()
            assert body.get("name") == 'gateway'
            assert body.get("id") != null
            assert body.get("label") == 'API Gateway'
    }

    def "Posting Channel object as non ADMIN user"() {
        given: "Logged user doesn't have ADMIN role"
            setupLoggedInUser('USER')
        and: "A Channel object with name=gateway, label=API Gateway and topic=api,gateway"
            var channel = """{
                            "name": "gateway",
                            "label": "API Gateway",
                            "topics": "api,gateway"
                        }"""
        when: "Channel is posted to /channel endpoint"
            var response = webTestClient.post()
                    .uri('/channel')
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .body(Mono.just(channel), String.class)
                    .exchange()
        then: "Response will return Forbidden"
            response.expectStatus().isForbidden()

    }

    def "Get Channel after creating channel"() {
        given: "A Channel object with name=gateway, label=API Gateway and topic=api,gateway is created"
            setupLoggedInUser('USER')
            var channel = Channel.ChannelBuilder.builder()
                    .name("general")
                    .label("General")
                    .build()
            var savedChannel = channelRepository.save(channel).block()

        when: "GET /channel/id will return the created channel"
            var response = webTestClient.get()
                    .uri('/channel/' + savedChannel.getId())
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .exchange()
                    .returnResult(Map.class)
        then: "Returned Object will contained Channel name=general"
            Map<String, String> body = response.responseBody.blockFirst()
            assert body.get("name") == 'general'
            assert body.get("id") == savedChannel.getId()
            assert body.get("label") == 'General'
    }

    def "Add user to channel"() {
        given: "User Shaun exists"
            setupLoggedInUser('USER')
            var shaun = new ApplicationUser("shaun", "shaun@test.com", List.of( "USER"))
            var user = userRepository.save(shaun).block()
        and: "Channel exists"
            var channel = Channel.ChannelBuilder.builder()
                    .name("general")
                    .label("General")
                    .build()
            var savedChannel = channelRepository.save(channel).block()
        when: "User is added to channel"
            var postResponse = webTestClient.post()
                .uri('/channel/' + savedChannel.getId() + '/user')
                .header("Content-Type", "application/json")
                .header('Authorization', token)
                .body(Mono.just(String.format("""{ "id": "%s"}""", user.getId())), String.class)
                .exchange()
        then: "Post response should return 200"
            postResponse.expectStatus().isOk()
    }

    def "Subscribe loggedIn user to channel"() {
        given: "Channel exists"
            setupLoggedInUser('USER')
            var channel = Channel.ChannelBuilder.builder()
                    .name("general")
                    .label("General")
                    .build()
            var savedChannel = channelRepository.save(channel).block()
        when: "User is added to channel"
            var postResponse = webTestClient.post()
                    .uri('/channel/' + savedChannel.getId() + '/subscribe')
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .exchange()
        then: "Post response should return 200"
            postResponse.expectStatus().isOk()
    }

    def "Add post to channel"() {
        given: "User is subscribed to channel"
            setupLoggedInUser('USER')
            var user = new ApplicationUser("test", "test@test.com", List.of("USER"))
            var savedUser = userRepository.save(user).block()
        and: "Channel exists"
            var channel = Channel.ChannelBuilder.builder()
                .name("general")
                .label("General")
                .userIds(List.of(savedUser.getId()))
                .build()
            var savedChannel = channelRepository.save(channel).block()

        when: "User post to channel"
            var postResponse = webTestClient.post()
                .uri('/channel/' + savedChannel.getId() + '/post')
                .header("Content-Type", "application/json")
                .header('Authorization', token)
                .body(Mono.just("""{"content": "This is a channel post!"}"""), String.class)
                .exchange()
                .returnResult(Map.class)

        then: "Response returns 200"
            Map<String, String> body = postResponse.responseBody.blockFirst()
            assert body != null
            assert body.get("content") != null
    }

    def "Add post to channel for user not subscribed to channel"() {
        setupLoggedInUser('USER')
        given: "User is present "
            var user = new ApplicationUser("test", "test@test.com", List.of("USER"))
            userRepository.save(user).block()
        and: "Channel exists, but user not subscribed"
            var channel = Channel.ChannelBuilder.builder()
                    .name("general")
                    .label("General")
                    .build()
            var savedChannel = channelRepository.save(channel).block()

        when: "User post to channel"
            var postResponse = webTestClient.post()
                    .uri('/channel/' + savedChannel.getId() + '/post')
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .body(Mono.just("""{"content": "This is a channel post!"}"""), String.class)
                    .exchange()

        then: "Response returns 403"
            postResponse.expectStatus().isForbidden()
    }

    def "Add reply to post on a channel"() {
        setupLoggedInUser('USER')
        given: "User is subscribed to channel"
            var user = new ApplicationUser("test", "test@test.com", List.of("USER"))
            var savedUser = userRepository.save(user).block()

        and: "Channel exists"
            var channel = Channel.ChannelBuilder.builder()
                    .name("general")
                    .label("General")
                    .userIds(List.of(savedUser.getId()))
                    .build()
            var savedChannel = channelRepository.save(channel).block()

        and: "User post exists"
            var post = ChannelPost.builder()
                        .channelId(savedChannel.getId())
                        .content("Parent post")
                        .senderId(savedUser.getId())
                        .build()

            var parentPost = postRepository.save(post).block()

        when: "User post replies to channel"
            var postResponse = webTestClient.post()
                .uri('/channel/' + savedChannel.getId() + '/post/'+ parentPost.getId() + "/replies")
                .header("Content-Type", "application/json")
                .header('Authorization', token)
                .body(Mono.just("""{"content": "This is a reply to parent post!"}"""), String.class)
                .exchange()
                .returnResult(Map.class)

        then: "Response returns 200"
            Map<String, String> body  = postResponse.responseBody.blockFirst()
            assert body != null
            assert body.get("content") != null
    }

    def cleanup() {
        userRepository.deleteAll().block()
        channelRepository.deleteAll().block()
    }
}
