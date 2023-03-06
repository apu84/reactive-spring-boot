package com.chatty.api;

import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

public class ResponseUtils {
    static ServerResponse.BodyBuilder ok() {
        return ServerResponse.ok().contentType(APPLICATION_JSON);
    }

    static RequestPredicate get(final String param) {
        return GET(param).and(accept(APPLICATION_JSON));
    }

    static RequestPredicate post(final String param) {
        return POST(param).and(accept(APPLICATION_JSON));
    }

    static RequestPredicate put(final String param) {
        return PUT(param).and(accept(APPLICATION_JSON));
    }

    static RequestPredicate delete(final String param) {
        return DELETE(param).and(accept(APPLICATION_JSON));
    }

    static URI entityURI(final ServerRequest request, final String entityId) {
        return request.uri().resolve(entityId);
    }
}
