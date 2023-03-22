package com.chatty.core.security;

import com.chatty.ApplicationConfiguration;
import com.chatty.core.exception.UnauthorizedException;
import com.chatty.core.user.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public class AuthWebClient {
    private final WebClient webClient;

    @Autowired
    public AuthWebClient(ApplicationConfiguration applicationConfiguration) {
        this.webClient = WebClient.create(applicationConfiguration.getAuthServer());
    }
    public Mono<ApplicationUser> validate(final String token) {
        return webClient.get()
                .uri("/auth/me")
                .accept(APPLICATION_JSON)
                .header("Authorization", token)
                .exchangeToMono(clientResponse -> {
                    // TODO: Add Logging for the client response
                    if (clientResponse.statusCode().value() == 200) {
                        return clientResponse
                                .bodyToMono(ApplicationUser.class);
                    } else if (clientResponse.statusCode().value() == 401 ||
                            clientResponse.statusCode().value() == 403) {
                        throw new UnauthorizedException("Invalid user");
                    } else {
                        throw new IllegalArgumentException("Auth server returned: " + clientResponse.statusCode().value());
                    }
                });
    }
}
