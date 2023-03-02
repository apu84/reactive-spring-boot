package demo.employee;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EmployeeMongoDBRepository extends ReactiveCrudRepository<EmployeeImp, String> {
}
