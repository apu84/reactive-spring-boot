package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;

public interface ChannelPostEventListener {
    void onData(ChannelPost post);
    void processComplete();
}
