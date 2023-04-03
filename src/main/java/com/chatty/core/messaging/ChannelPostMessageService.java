package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ChannelPostMessageService {
    private final KafkaSender<String, Object> kafkaSender;
    private final ReceiverOptions<String, Object> receiverOptions;
    private final Map<String, Sinks.Many<ServerSentEvent<Object>>> topicSink = new HashMap<>();
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

    private Sinks.Many<ServerSentEvent<Object>> consume(Topic topic) {
        var options = receiverOptions.subscription(Collections.singleton(topic.toString()))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        KafkaReceiver.create(options).receive()
                .log()
                .subscribe(record -> topicSink.get(topic.toString()).tryEmitNext(toServerSentEvent(record)));
        return topicSink.get(topic.toString());
    }

    public Flux<ServerSentEvent<Object>> consumeMessage(Topic topic) {
         var sink = topicSink.get(topic.toString());
         if(sink == null) {
            topicSink.put(topic.toString(), Sinks.many().multicast().onBackpressureBuffer());
            sink = consume(topic);
         }
         return sink.asFlux();
    }
    static ServerSentEvent<Object> toServerSentEvent(ConsumerRecord<String, Object> consumerRecord) {
        return ServerSentEvent.builder()
                .event(consumerRecord.topic())
                .data(consumerRecord.value())
                .id(String.valueOf(consumerRecord.timestamp()))
                .build();
    }

}
