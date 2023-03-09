package com.chatty.core.security;

import com.chatty.core.user.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class AuthFilter implements WebFilter {
    WebClient webClient;

    public AuthFilter() {
        webClient = WebClient.create("http://localhost:8899");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token)) {
            return isValid(token)
                    .flatMap(user -> {
                        Collection<GrantedAuthority> grantedAuthorities = user.getRoles().stream().map(r -> new GrantedAuthority() {
                            @Override
                            public String getAuthority() {
                                return r;
                            }
                        }).collect(Collectors.toList());
                        Authentication authentication
                                = new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(user.getUserName(), token, grantedAuthorities), token, grantedAuthorities);
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    }).onErrorResume((e) -> sendUnauthenticated(exchange.getResponse()));
        }
        return chain.filter(exchange);
    }



    private Mono<Void> sendUnauthenticated(final ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private Mono<User> isValid(final String token) {
        return webClient.get()
                .uri("/auth/me")
                .accept(APPLICATION_JSON)
                .header("Authorization", token)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().value() == 200) {
                        return clientResponse.bodyToMono(User.class);
                    }
                    return clientResponse.createError();
                });
    }
    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }
        return null;
    }
}
