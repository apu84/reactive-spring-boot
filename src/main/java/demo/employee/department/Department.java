package demo.employee.department;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.UUID;

public class Department {
    @Id
    private String id;
    private String name;
    private String label;

    private Date created;
    private Date lastModified;

    public Department(String name, String label) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.label = label;
        this.created = this.lastModified = new Date();
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        this.lastModified = new Date();
    }

    public String getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "Department { name: '" + name + "', label: '" + label + "'}";
    }
}
