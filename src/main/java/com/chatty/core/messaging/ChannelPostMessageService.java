package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ChannelPostMessageService {
    private final KafkaTemplate<String, ChannelPost> kafkaTemplate;
    private final KafkaConfig kafkaConfig;
    public ChannelPostMessageService(final KafkaTemplate<String, ChannelPost> kafkaTemplate,
                                     final KafkaConfig kafkaConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaConfig = kafkaConfig;
    }
    public CompletableFuture<SendResult<String, ChannelPost>> sendMessage(ChannelPost post) {
        return kafkaTemplate.send(kafkaConfig.getTopic(), post);
    }
}
