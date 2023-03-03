package demo.employee;

import demo.employee.department.Department;
import demo.employee.department.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class EmployeeHandler {
    DepartmentRepository departmentRepository;
    EmployeeMongoDBRepository employeeMongoDBRepository;

    @Autowired
    EmployeeHandler(final DepartmentRepository departmentRepository,
                    final EmployeeMongoDBRepository employeeMongoDBRepository) {
        this.departmentRepository = departmentRepository ;
        this.employeeMongoDBRepository = employeeMongoDBRepository;
    }

    Mono<ServerResponse> get(final ServerRequest serverRequest) {
        final String id = serverRequest.pathVariable("id");
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(employeeMongoDBRepository.findById(id).flatMap(emp -> decorate(emp)), EmployeeView.class);
    }

    Mono<ServerResponse> create(final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(EmployeeImp.class).flatMap((emp) -> {
            final EmployeeImp employee = new EmployeeImp(emp.getName(), emp.getAge(), emp.getDepartmentId());
            return employeeMongoDBRepository.save(employee)
                    .flatMap(saved -> {
                        final URI employeeURI = URI.create("/employee/" + saved.getId());
                        return ServerResponse.created(employeeURI).body(BodyInserters.fromValue(saved));
                    });
        });
    }

    Mono<ServerResponse> getAll(final ServerRequest serverRequest) {
        Flux<EmployeeView> all = employeeMongoDBRepository.findAll().flatMap((ins) -> decorate(ins));
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(all, EmployeeView.class);
    }

    Mono<EmployeeView> decorate(final Employee employee) {
        Mono<Department> departmentMono = departmentRepository
                .findById(employee.getDepartmentId());
        return departmentMono.map((dept) -> {
           EmployeeView employeeView = new EmployeeViewImpl(employee, dept);
           return employeeView;
        });
    }

    Mono<ServerResponse> update(final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(EmployeeImp.class)
                .flatMap((emp) ->
                        employeeMongoDBRepository.findById(emp.getId()).flatMap((toUpdate) -> {
                            toUpdate.setAge(emp.getAge());
                            toUpdate.setDepartmentId(emp.getDepartmentId());
                            return employeeMongoDBRepository.save(toUpdate)
                                    .flatMap((e) -> ServerResponse.noContent().build());
                        })
                );
    }

    Mono<ServerResponse> delete(final ServerRequest serverRequest) {
        final String id = serverRequest.pathVariable("id");
        return employeeMongoDBRepository.deleteById(id).flatMap((e) -> ServerResponse.ok().build());
    }
}
