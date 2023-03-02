package demo.employee;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Deprecated
public class EmployeeRepository {
  List<EmployeeImp> mEmployees = new ArrayList<>();

  public EmployeeImp get(final String pId) {
    return findById(pId);
  }

  public Mono<Void> save(final EmployeeImp pEmployee) {
    mEmployees.add(pEmployee);
    return Mono.empty();
  }

  public List<EmployeeImp> getAll() {
    return mEmployees;
  }

  public EmployeeImp update(final EmployeeImp employee) {
    final EmployeeImp updateEmployee = findById(employee.getId());
    updateEmployee.setAge(employee.getAge());
    return updateEmployee;
  }

  public void remove(final EmployeeImp employee) {
    Iterator<EmployeeImp> employeeIterator = mEmployees.stream().iterator();
    while(employeeIterator.hasNext()) {
      EmployeeImp savedEmployee = employeeIterator.next();
      if (savedEmployee.getId().equals(employee.getId())) {
        employeeIterator.remove();
      }
    }
  }

  private EmployeeImp findById(final String employeeId) {
    return mEmployees.stream()
            .filter(pEmployee -> pEmployee.getId().equals(employeeId))
            .findFirst()
            .orElseThrow();
  }
}
