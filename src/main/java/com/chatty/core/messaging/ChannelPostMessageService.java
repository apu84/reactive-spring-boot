package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.time.Instant;
import java.util.Collections;

@Slf4j
@Service
public class ChannelPostMessageService {

    private final KafkaSender<String, Object> kafkaSender;
    private final ReceiverOptions<String, Object> receiverOptions;

    @Autowired
    public ChannelPostMessageService(final KafkaSender<String, Object> kafkaSender,
                                     final ReceiverOptions<String, Object> receiverOptions) {
        this.kafkaSender = kafkaSender;
        this.receiverOptions = receiverOptions;
    }

    public Mono<SenderResult<ChannelPost>> sendMessage(Mono<Topic> topicMono, ChannelPost post) {
        return topicMono.flatMap(topic -> sendMessage(topic, post));
    }

    private Mono<SenderResult<ChannelPost>> sendMessage(Topic topic, ChannelPost channelPost) {
        log.info("Sending to topic={}, {}={}", topic, ChannelPost.class.getSimpleName(), channelPost);
        return kafkaSender
                .send(Mono.just(channelPost).map(post -> SenderRecord.create(new ProducerRecord<>(topic.toString(), post), post)))
                .doOnError(error -> log.error("Failed to send message: ", error.getCause()))
                .doOnNext(record -> {
                    RecordMetadata recordMetadata = record.recordMetadata();
                    Instant timestamp = Instant.ofEpochMilli(recordMetadata.timestamp());
                    log.info(String.format("Message %s sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                            record.correlationMetadata(),
                            recordMetadata.topic(),
                            recordMetadata.partition(),
                            recordMetadata.offset(),
                            timestamp.toString()));
                }).next();
    }

    public Flux<Object> consumeMessage(Topic topic) {
        ReceiverOptions<String, Object> options = receiverOptions.subscription(Collections.singleton(topic.toString()))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        return KafkaReceiver.create(options).receive().map(ConsumerRecord::value);
    }
}
