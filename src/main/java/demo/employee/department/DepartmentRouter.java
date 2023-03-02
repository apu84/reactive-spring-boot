package demo.employee.department;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class DepartmentRouter {
    @Bean
    public RouterFunction<ServerResponse> departmentRoute(final DepartmentHandler departmentHandler) {
        return RouterFunctions
                .nest(path("/department"),
                        route(GET("/{id}").and(accept(APPLICATION_JSON)), departmentHandler::get)
                                .andRoute(POST("/").and(accept(APPLICATION_JSON)), departmentHandler::create)
                                .andRoute(PUT("/{id}").and(accept(APPLICATION_JSON)), departmentHandler::update)
                                .andRoute(DELETE("/{id}").and(accept(APPLICATION_JSON)), departmentHandler::delete))
                .andRoute(GET("/departments").and(accept(APPLICATION_JSON)), departmentHandler::getAll);
    }
}
