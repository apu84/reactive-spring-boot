package com.chatty

import com.chatty.core.security.AuthWebClient
import com.chatty.core.user.ApplicationUser
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import spock.lang.Specification

@ContextConfiguration()
@AutoConfigureDataMongo
@ComponentScan(["com.chatty"])
class BaseSpecification extends Specification {
    @Autowired
    WebTestClient webTestClient;
    @SpringBean
    AuthWebClient authWebClient = Stub()

    String token = "Bearer token"

    def setupLoggedInUser(String name, String email, String role) {
        var loggedInUser = ApplicationUser.builder()
                .username(name)
                .email(email)
                .roles(List.of("ROLE_" + role))
                .build()
        authWebClient.validate(token) >> Mono.just(loggedInUser)
    }

    def setupLoggedInUser(String role) {
        setupLoggedInUser("test", "test@test.com", role)
    }

    def setupLoggedInUserAsAdmin() {
        setupLoggedInUser("ADMIN")
    }
}
