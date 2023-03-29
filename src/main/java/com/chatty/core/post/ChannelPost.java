package com.chatty.core.post;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ChannelPost extends Post {
    private String channelId;
}
