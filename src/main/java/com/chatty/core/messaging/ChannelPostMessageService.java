package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ChannelPostMessageService {
    private final KafkaTemplate<String, ChannelPost> kafkaTemplate;
    public ChannelPostMessageService(final KafkaTemplate<String, ChannelPost> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    CompletableFuture<SendResult<String, ChannelPost>> sendMessage(String topic, ChannelPost post) {
        return kafkaTemplate.send(topic, post);
    }
}
