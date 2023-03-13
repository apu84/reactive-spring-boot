package com.chatty.core.user;

import org.springframework.data.annotation.Id;


import javax.management.relation.Role;
import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.notNull;

public class ApplicationUser {
    @Id
    private String id;
    private String username;
    private String email;
    private String avatar;
    private Status userStatus;
    enum Status {
        ACTIVE,
        INACTIVE,
    }
    private Availability availability;
    enum Availability {
        ONLINE,
        OFFLINE
    }
    private Date created;
    private Date lastModified;
    private List<String> roles;

    public ApplicationUser() {}

    public ApplicationUser(final String username, final String email, final List<String> roles) {
        notNull(email, "email is empty");
        notNull(username, "UserName is empty");
        this.username = username;
        this.email = email;
        this.created = new Date();
        this.lastModified = new Date();
        this.userStatus = Status.ACTIVE;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public String getAvatar() {
        return avatar;
    }

    public Status getUserStatus() {
        return userStatus;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public Availability getAvailability() {
        return availability;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyUpdate();
    }

    public void setUserStatus(Status userStatus) {
        this.userStatus = userStatus;
        notifyUpdate();
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
        notifyUpdate();
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void notifyUpdate() {
        lastModified = new Date();
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return String.format("ApplicationUser { id: %s, userName: %s, name: %s, created: %s, lastModified: %s }",
                id, email, username, created, lastModified);
    }
}
