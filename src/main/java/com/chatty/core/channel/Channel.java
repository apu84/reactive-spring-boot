package com.chatty.core.channel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.*;

@Builder(toBuilder = true)
@Getter
@ToString
public class Channel {
    @Id
    String id;
    @NonNull
    String name;
    @NonNull
    String spaceId;
    String label;
    String topics;
    @Builder.Default
    Set<String> userIds = new HashSet<>();
    @Builder.Default
    Set<String> postIds = new HashSet<>();
    @Builder.Default
    Date created = new Date();
    @Builder.Default
    Date lastModified = new Date();
}
