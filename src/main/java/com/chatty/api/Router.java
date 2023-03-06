package com.chatty.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.chatty.api.ResponseUtils.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class Router {
    @Bean
    public RouterFunction<ServerResponse> userRouters(final UserHandler userHandler) {
        return RouterFunctions
                .nest(path("/user"),
                        route(get("/{id}"), userHandler::get)
                                .andRoute(post("/"), userHandler::create)
                                .andRoute(put("/{id}"), userHandler::update)
                                .andRoute(delete("/{id}"), userHandler::delete)
                )
                .andRoute(get("/users"), userHandler::getAll);
    }
}
