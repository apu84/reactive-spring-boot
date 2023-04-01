package com.chatty.core.post;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder()
@ToString
@Getter
//Jacksonized to deserialize lombok object properly
@Jacksonized
public class ChannelPost extends Post {
    private String channelId;
}
