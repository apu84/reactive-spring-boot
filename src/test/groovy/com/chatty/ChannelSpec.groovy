package com.chatty

import com.chatty.core.channel.ChannelController
import com.chatty.core.channel.ChannelRepository
import com.chatty.core.security.AuthWebClient
import com.chatty.core.user.ApplicationUser
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import reactor.core.publisher.Mono

@WebFluxTest(
        controllers= ChannelController.class
)
class ChannelSpec extends BaseSpecification {
    @SpringBean
    AuthWebClient authWebClient = Stub()

    @Autowired
    ChannelRepository  channelRepository

    String token = "Bearer token"

    def setup() {
        var loggedInUser = new ApplicationUser("test", "test@test.com", List.of("ADMIN", "USER"))
        authWebClient.validate(token) >> Mono.just(loggedInUser)
    }

    def "Posting Channel object persists channel"() {
        given: "A Channel object with name=gateway, label=API Gateway and topic=api,gateway"
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

    def "Get Channel after creating channel"() {
        given: "A Channel object with name=gateway, label=API Gateway and topic=api,gateway is created"
        var channel = """{
                        "name": "gateway",
                        "label": "API Gateway",
                        "topics": "api,gateway"
                    }"""
        var postResponse = webTestClient.post()
                .uri('/channel')
                .header("Content-Type", "application/json")
                .header('Authorization', token)
                .body(Mono.just(channel), String.class)
                .exchange()
                .returnResult(Map.class)
        Map<String, String> responseBody = postResponse.responseBody.blockFirst()
        var channelId = responseBody.get("id")
        assert channelId != null

        when: "GET /channel/id will return the created channel"
        var response = webTestClient.get()
                .uri('/channel/' + channelId)
                .header("Content-Type", "application/json")
                .header('Authorization', token)
                .exchange()
                .returnResult(Map.class)
        then: "Returned Object will contained Channel name=gateway"
            Map<String, String> body = response.responseBody.blockFirst()
            assert body.get("name") == 'gateway'
            assert body.get("id") == channelId
            assert body.get("label") == 'API Gateway'
    }

    def cleanup() {
        channelRepository.deleteAll().block()
    }
}
