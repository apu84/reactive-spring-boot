package demo.employee;

import java.util.Date;

public interface Employee {
    String getId();
    String getName();
    long getAge();
    Date getCreated();
    Date getLastUpdated();
    String getDepartmentId();
}
