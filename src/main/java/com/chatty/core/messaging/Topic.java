package com.chatty.core.messaging;

import lombok.Builder;
import lombok.NonNull;

import static org.springframework.util.StringUtils.hasText;

@Builder
public class Topic {
    private final static String SEPARATOR = ".";
    @NonNull
    String spaceName;
    String channelName;
    String userName;
    public String toString() {
        var topic = new StringBuilder();
        topic.append("Post").append(SEPARATOR).append(spaceName);
        if(hasText(channelName)) {
            topic.append(SEPARATOR).append(channelName);
        }
        if(hasText(userName)) {
            topic.append(SEPARATOR).append("userName");
        }
        return topic.toString();
    }
}
