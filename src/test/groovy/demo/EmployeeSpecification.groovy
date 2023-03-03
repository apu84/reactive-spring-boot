package demo

import demo.employee.Employee
import demo.employee.EmployeeHandler
import demo.employee.EmployeeImp
import demo.employee.EmployeeMongoDBRepository
import demo.employee.department.Department
import demo.employee.department.DepartmentRepository
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class EmployeeSpecification extends Specification {

    def "department should be present"() {
        given:
        def deptRepo = Stub(DepartmentRepository)
        def employeeRepo = Stub(EmployeeMongoDBRepository)
        def employeeHandler = new EmployeeHandler(deptRepo, employeeRepo)
        def request = Stub(ServerRequest)
        request.pathVariable("id") >> "1"
        employeeRepo.findById("1") >> Mono.just(new EmployeeImp("Autobot", 10, "1"))
        deptRepo.findById("1") >> Mono.just(new Department("sales", "Sales"))

        when:
        def response = employeeHandler.get(request)

        then:
        StepVerifier
                .create(response)
                .consumeNextWith { serverResponse ->
                    assert serverResponse.statusCode().value() == 200
                    assert serverResponse instanceof EntityResponse<Employee>
                    def emp = serverResponse.entity().block()
                    assert emp.name == 'Autobot'
                    assert emp.department.name == 'sales'
                }
                .expectComplete()
                .verify()
    }
}
