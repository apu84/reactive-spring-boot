package demo.employee;

import demo.employee.department.Department;

import java.util.Date;

public class EmployeeViewImpl implements EmployeeView {
    private Department department;
    private Employee employee;

    public EmployeeViewImpl(Employee employee, Department department) {
        this.employee = employee;
        this.department = department;
    }
    @Override
    public Department getDepartment() {
        return department;
    }

    @Override
    public String getId() {
        return employee.getId();
    }

    @Override
    public String getName() {
        return employee.getName();
    }

    @Override
    public long getAge() {
        return employee.getAge();
    }

    @Override
    public Date getCreated() {
        return employee.getCreated();
    }

    @Override
    public Date getLastUpdated() {
        return employee.getLastUpdated();
    }

    @Override
    public String getDepartmentId() {
        return employee.getDepartmentId();
    }
}
