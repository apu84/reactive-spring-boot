package demo.employee;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.UUID;

public class EmployeeImp implements Employee {
    @Id
    private String id;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getAge() {
        return age;
    }

    public void setAge(final long age) {
        this.age = age;
        lastUpdated = new Date();
    }

    private String name;
    private long age;

    public Date getCreated() {
        return created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    private Date created;
    private Date lastUpdated;

    public EmployeeImp(final String name, final long age, final String departmentId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
        this.departmentId = departmentId;
        this.created = new Date();
        this.lastUpdated = new Date();
    }

    private String departmentId;

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public String toString() {
        return "EmployeeImp: { id: " + id + ", name: " + name + ", salary: " + age + "}";
    }
}
