package demo.employee.department;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class DepartmentHandler {
    private DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentHandler(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    Mono<ServerResponse> get(final ServerRequest serverRequest) {
        final String id = serverRequest.pathVariable("id");
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(departmentRepository.findById(id), Department.class);
    }

    Mono<ServerResponse> create(final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Department.class).flatMap((dept) -> {
            final Department department = new Department(dept.getName(), dept.getLabel());
            return departmentRepository.save(department)
                    .flatMap(saved -> {
                        final URI departmentURI = URI.create("/department/" + saved.getId());
            return ServerResponse.created(departmentURI).body(BodyInserters.fromValue(saved));
        });
        });
    }

    Mono<ServerResponse> getAll(final ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(departmentRepository.findAll(), Department.class);
    }

    Mono<ServerResponse> update(final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Department.class)
                .flatMap((dept) ->
                        departmentRepository.findById(dept.getId()).flatMap((toUpdate) -> {
                            toUpdate.setLabel(dept.getLabel());
                            return departmentRepository.save(toUpdate)
                                    .flatMap((e) -> ServerResponse.noContent().build());
                        })
                );
    }

    Mono<ServerResponse> delete(final ServerRequest serverRequest) {
        final String id = serverRequest.pathVariable("id");
        return departmentRepository.deleteById(id).flatMap((e) -> ServerResponse.ok().build());
    }
}
