package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ReceiverOptions<String, Event<ChannelPost>> receiverOptions;
    private final Map<String, Sinks.Many<Event<ChannelPost>>> topicSink = new HashMap<>();
    private final EventService<ChannelPost> eventService;
    @Autowired
    public ChannelPostMessageService(final KafkaSender<String, Object> kafkaSender,
                                     final ReceiverOptions<String, Event<ChannelPost>> receiverOptions,
                                     final EventService<ChannelPost> eventService) {
        this.kafkaSender = kafkaSender;
        this.receiverOptions = receiverOptions;
        this.eventService = eventService;
    }

    public Mono<Event<ChannelPost>> sendMessage(Topic topic, Event<ChannelPost> channelPost) {
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
                })
                .next()
                .flatMap(record -> eventService.save(channelPost));
    }

    private Sinks.Many<Event<ChannelPost>> consume(Topic topic) {
        var options = receiverOptions.subscription(Collections.singleton(topic.toString()))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        KafkaReceiver.create(options).receive()
                .log()
                .subscribe(record -> topicSink.get(topic.toString()).tryEmitNext(record.value()));
        return topicSink.get(topic.toString());
    }

    public Flux<Event<ChannelPost>> consumeMessage(Topic topic) {
        var sink = topicSink.get(topic.toString());
        if (sink == null) {
            topicSink.put(topic.toString(), Sinks.many().multicast().onBackpressureBuffer());
            sink = consume(topic);
        }
        return sink.asFlux();
    }

    public Flux<Event<ChannelPost>> consumeMessage(Topic topic, String lastEventId) {
        var sink = topicSink.get(topic.toString());
        if (sink == null) {
            topicSink.put(topic.toString(), Sinks.many().multicast().onBackpressureBuffer());
            sink = consume(topic);
        }
        return Flux.concat(sink.asFlux(),eventService.eventsAfter(lastEventId));
    }
}
