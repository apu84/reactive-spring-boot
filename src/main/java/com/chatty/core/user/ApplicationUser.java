package com.chatty.core.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder(toBuilder = true)
@ToString
@Getter
public class ApplicationUser {
    @Id
    private String id;
    @NonNull
    private String username;
    @NonNull
    private String email;
    private String avatar;
    @Builder.Default
    private Status userStatus = Status.ACTIVE;
    enum Status {
        ACTIVE,
        INACTIVE,
    }
    private Availability availability;
    enum Availability {
        ONLINE,
        OFFLINE
    }
    @Builder.Default
    private Date created = new Date();
    @Builder.Default
    private Date lastModified = new Date();
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    @Builder.Default
    private List<String> channelIds = new ArrayList<>();
    @Builder.Default
    private List<String> spaceIds = new ArrayList<>();
}
