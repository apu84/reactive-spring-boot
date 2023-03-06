package com.chatty.core.user;

import org.springframework.data.annotation.Id;
import org.springframework.util.Assert;

import java.util.Date;

import static org.springframework.util.Assert.notNull;

public class User {
    @Id
    private String id;
    private String userName;
    private String name;
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

    public User(final String userName, final String name) {
        notNull(userName, "UserName is empty");
        notNull(name, "Name is empty");
        this.userName = userName;
        this.name = name;
        this.created = new Date();
        this.lastModified = new Date();
        this.userStatus = Status.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setName(String name) {
        this.name = name;
        notifyUpdate();
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
    @Override
    public String toString() {
        return String.format("User { id: %s, userName: %s, name: %s, created: %s, lastModified: %s }",
                id, userName, name, created, lastModified);
    }
}
