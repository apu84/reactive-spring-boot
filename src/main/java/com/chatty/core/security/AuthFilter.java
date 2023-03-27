package com.chatty.core.security;

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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication;

public class AuthFilter extends AuthenticationWebFilter {
    AuthWebClient authWebClient;

    private UserRepository userRepository;
    public AuthFilter(UserRepository userRepository,
                      AuthWebClient webClient) {
        super((ReactiveAuthenticationManager) Mono::just);
        this.userRepository = userRepository;
        this.authWebClient = webClient;
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
        return authWebClient.validate(token).flatMap(this::getUser);
    }

    Mono<ApplicationUser> getUser(ApplicationUser authUser) {
        return userRepository
                .findUserByEmail(authUser.getEmail())
                .switchIfEmpty(saveUser(authUser));
    }

    Mono<ApplicationUser> saveUser(ApplicationUser authUser) {
        ApplicationUser applicationUser = ApplicationUser.builder()
                .username(authUser.getUsername())
                .email(authUser.getEmail())
                .roles(authUser.getRoles())
                .build();
        return userRepository.save(applicationUser);
    }
    private String resolveToken(ServerHttpRequest request) {
        return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    }
}
