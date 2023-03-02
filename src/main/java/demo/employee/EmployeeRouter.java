package demo.employee;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class EmployeeRouter {
    @Bean
    public RouterFunction<ServerResponse> employeeRoute(final EmployeeHandler employeeHandler) {
        return RouterFunctions
                .nest(path("/employee"),
                        route(GET("/{id}").and(accept(APPLICATION_JSON)), employeeHandler::get)
                                .andRoute(POST("/").and(accept(APPLICATION_JSON)), employeeHandler::create)
                                .andRoute(PUT("/{id}").and(accept(APPLICATION_JSON)), employeeHandler::update)
                                .andRoute(DELETE("/{id}").and(accept(APPLICATION_JSON)), employeeHandler::delete))
                .andRoute(GET("/employees").and(accept(APPLICATION_JSON)), employeeHandler::getAll);
    }
}
