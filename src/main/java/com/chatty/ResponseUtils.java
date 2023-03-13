package com.chatty;

import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

public class ResponseUtils {
    public static ServerResponse.BodyBuilder ok() {
        return ServerResponse.ok().contentType(APPLICATION_JSON);
    }

    public static RequestPredicate get(final String param) {
        return GET(param).and(accept(APPLICATION_JSON));
    }

    public static RequestPredicate post(final String param) {
        return POST(param).and(accept(APPLICATION_JSON));
    }

    public static RequestPredicate put(final String param) {
        return PUT(param).and(accept(APPLICATION_JSON));
    }

    public static RequestPredicate delete(final String param) {
        return DELETE(param).and(accept(APPLICATION_JSON));
    }

    public static URI entityURI(final ServerRequest request, final String entityId) {
        return request.uri().resolve(entityId);
    }
}
