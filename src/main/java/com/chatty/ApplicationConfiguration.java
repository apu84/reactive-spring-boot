package com.chatty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
    }
}
