package com.chatty.core.post;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder()
@ToString
@Getter
public class ChannelPost extends Post {
    private String channelId;
}
