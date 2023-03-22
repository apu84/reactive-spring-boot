package com.chatty.core.security;

import com.chatty.ApplicationConfiguration;
import com.chatty.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    private final UserRepository userRepository;
    private final AuthWebClient authWebClient;
    @Autowired
    public SecurityConfig(ApplicationConfiguration applicationConfiguration,
                          UserRepository userRepository,
                          AuthWebClient authWebClient) {
        this.userRepository = userRepository;
        this.authWebClient = authWebClient;
    }
    @Bean
    SecurityWebFilterChain springWebFilterChain(final ServerHttpSecurity http) {
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();
        http.logout().disable();

        http.authorizeExchange()
                .pathMatchers("/health-check/**")
                .permitAll();

        http.authorizeExchange()
                .pathMatchers("/**").authenticated()
                .and()
                .addFilterAt(new AuthFilter(userRepository, authWebClient), SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                .cors();
        return http.build();
    }
}
