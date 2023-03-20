package com.chatty.core.security;

import com.chatty.ApplicationConfiguration;
import com.chatty.core.exception.UnauthorizedException;
import com.chatty.core.user.ApplicationUser;
import com.chatty.core.user.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication;

public class AuthFilter extends AuthenticationWebFilter {
    WebClient webClient;

    private UserRepository userRepository;
    public AuthFilter(ApplicationConfiguration applicationConfiguration,
                      UserRepository userRepository) {
        super((ReactiveAuthenticationManager) Mono::just);
        webClient = WebClient.create(applicationConfiguration.getAuthServer());
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token)) {
            return validate(token)
                    .flatMap(user -> chain.filter(exchange)
                                .contextWrite(withAuthentication(toAuthentication(user, token))));
        }
        return chain.filter(exchange);
    }

    private Authentication toAuthentication(final ApplicationUser applicationUser, final String token) {
        UserDetails user = User.builder()
                .username(applicationUser.getEmail())
                .password(token)
                .authorities(
                        applicationUser.getRoles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()))
                .build();
        return new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
    }

    private Mono<ApplicationUser> validate(final String token) {
        return webClient.get()
                .uri("/auth/me")
                .accept(APPLICATION_JSON)
                .header("Authorization", token)
                .exchangeToMono(clientResponse -> {
                    // TODO: Add Logging for the client response
                    if (clientResponse.statusCode().value() == 200) {
                        return clientResponse
                                .bodyToMono(ApplicationUser.class)
                                .flatMap(this::getUser);
                    } else if (clientResponse.statusCode().value() == 401 ||
                            clientResponse.statusCode().value() == 403) {
                        throw new UnauthorizedException("Invalid user");
                    } else {
                        throw new IllegalArgumentException("Auth server returned: " + clientResponse.statusCode().value());
                    }
                });
    }

    Mono<ApplicationUser> getUser(ApplicationUser authUser) {
        return userRepository
                .findUserByEmail(authUser.getEmail())
                .switchIfEmpty(saveUser(authUser));
    }

    Mono<ApplicationUser> saveUser(ApplicationUser authUser) {
        ApplicationUser applicationUser
                = new ApplicationUser(authUser.getUsername(), authUser.getEmail(), authUser.getRoles());
        return userRepository.save(applicationUser);
    }
    private String resolveToken(ServerHttpRequest request) {
        return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    }
}
