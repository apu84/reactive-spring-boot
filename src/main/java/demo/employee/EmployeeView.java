package demo.employee;

import demo.employee.department.Department;

public interface EmployeeView extends Employee {
    Department getDepartment();
}
