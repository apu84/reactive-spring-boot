package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder()
@Jacksonized
@Getter
@NoArgsConstructor
public class ChannelPostEvent extends Event<ChannelPost> {
}
