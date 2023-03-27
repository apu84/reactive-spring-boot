package com.chatty

import com.chatty.core.channel.ChannelRepository
import com.chatty.core.space.Space
import com.chatty.core.space.SpaceController
import com.chatty.core.space.SpaceRepository
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import reactor.core.publisher.Mono

@WebFluxTest(controllers = SpaceController.class)
class SpaceSpec extends BaseSpecification {
    @Autowired
    SpaceRepository spaceRepository
    @Autowired
    ChannelRepository channelRepository

    def "Post Space object persists"() {
        given: "Admin user logged in"
            setupLoggedInUserAsAdmin()

        and: "Space object present"
            var space = """{ "name": "summertime",
                                    "label": "Summer Time"
                                    }"""
        when: "Posting space object to /space endpoint"
            var response = webTestClient.post()
                    .uri("/space")
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .body(Mono.just(space), String.class)
                    .exchange()

        then:
            response.expectStatus().isCreated()
            Map<String, String> body = response.returnResult(Map.class).responseBody.blockFirst()
            assert body.get("name") == 'summertime'
            assert body.get("label") == "Summer Time"
    }

    def "Add channel to space"() {
        given: "Loggedin as Admin user"
            setupLoggedInUser("SPACE_ADMIN")
        and: "Space named summertime exists"
            var space = Space.builder()
                                .name("summertime")
                                .label("Summer Time")
                                .build()
            var savedSpace = spaceRepository.save(space).block()
        when: "Channel is posted to Space"
            var channel = """{
                                    "name": "common",
                                    "label": "Common",
                                    "topics": "Common topics"
                                }"""

            var response = webTestClient.post()
                    .uri("/space/" + savedSpace.getId() + "/channel")
                    .header("Content-Type", "application/json")
                    .header('Authorization', token)
                    .body(Mono.just(channel), String.class)
                    .exchange()

        then: "Channel is created and channel has a space id"
            response.expectStatus().isOk()
            Map<String, String> body = response.returnResult(Map.class).responseBody.blockFirst()
            assert body.get("name") == "common"
            assert body.get("label") == "Common"
            assert body.get("spaceId") == space.getId()
    }

    def setup() {}
    def cleanup(){
        spaceRepository.deleteAll()
        channelRepository.deleteAll()
    }
}
