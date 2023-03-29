package com.chatty

import com.chatty.core.channel.Channel
import com.chatty.core.channel.ChannelController
import com.chatty.core.channel.ChannelRepository
import com.chatty.core.post.ChannelPost
import com.chatty.core.post.PostRepository
import com.chatty.core.user.ApplicationUser
import com.chatty.core.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import reactor.core.publisher.Mono

@WebFluxTest(controllers= ChannelController.class)
class ChannelSpec extends BaseSpecification {

    @Autowired
    ChannelRepository  channelRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    PostRepository postRepository

    def setup() {
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
            var channel = Channel.builder()
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

    def "PUT Channel object updates channel"() {
        given: "A Channel object with name=gateway, label=API Gateway and topic=api,gateway exists"
            setupLoggedInUserAsAdmin()
            var channel = Channel.builder()
                    .name("general")
                    .label("General")
                    .build()
            var savedChannel = channelRepository.save(channel).block()

        when: "Channel label is changed to FooBar"
            var updateChannel = """{
                            "name": "gateway",
                            "label": "FooBar",
                            "topics": "api,gateway"
                        }"""

            var response = webTestClient.put()
                    .uri('/channel/' + savedChannel.getId())
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .body(Mono.just(updateChannel), String.class)
                    .exchange()
                    .returnResult(Map.class)

        then: "Returned Object will contained Channel with label=FooBar"
            Map<String, String> body = response.responseBody.blockFirst()
            assert body.get("name") == 'gateway'
            assert body.get("id") == savedChannel.getId()
            assert body.get("label") == 'FooBar'
    }

    def "PUT Channel object to updates channel as non admin user"() {
        given: "A Channel object with name=gateway, label=API Gateway and topic=api,gateway exists"
            setupLoggedInUser('USER')
            var channel = Channel.builder()
                    .name("general")
                    .label("General")
                    .build()
            var savedChannel = channelRepository.save(channel).block()

        when: "Channel label is changed to FooBar"
            var updateChannel = """{
                                "name": "gateway",
                                "label": "FooBar",
                                "topics": "api,gateway"
                            }"""

            var response = webTestClient.put()
                    .uri('/channel/' + savedChannel.getId())
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .body(Mono.just(updateChannel), String.class)
                    .exchange()

        then: "Returned response will be 403"
            response.expectStatus().isForbidden()
    }

    def "Add user to channel"() {
        given: "User Shaun exists"
            setupLoggedInUser('USER')
            var shaun = ApplicationUser.builder()
                    .username("shaun")
                    .email("shaun@test.com")
                    .roles(List.of("USER"))
                    .build()
            var user = userRepository.save(shaun).block()
        and: "Channel exists"
            var channel = Channel.builder()
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
            var channel = Channel.builder()
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
            var user = ApplicationUser.builder()
                    .username("test")
                    .email("test@test.com")
                    .roles(List.of("USER"))
                    .build()
            var savedUser = userRepository.save(user).block()
        and: "Channel exists"
            var channel = Channel.builder()
                .name("general")
                .label("General")
                .userIds(Set.of(savedUser.getId()))
                .spaceId("some_dummy_space")
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
            var user = ApplicationUser.builder()
                    .username("test")
                    .email("test@test.com")
                    .roles(List.of("USER"))
                    .build()
            userRepository.save(user).block()
        and: "Channel exists, but user not subscribed"
            var channel = Channel.builder()
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
            var user = ApplicationUser.builder()
                    .username("test")
                    .email("test@test.com")
                    .roles(List.of("USER"))
                    .build()
            var savedUser = userRepository.save(user).block()

        and: "Channel exists"
            var channel = Channel.builder()
                    .name("general")
                    .label("General")
                    .userIds(Set.of(savedUser.getId()))
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
