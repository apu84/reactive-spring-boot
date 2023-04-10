package com.chatty.core.messaging;

import com.chatty.core.post.ChannelPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.SynchronousSink;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.springframework.util.StringUtils.hasText;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

@Slf4j
@Service
public class ChannelPostMessageService {
    private final KafkaSender<String, Object> kafkaSender;
    private final ReceiverOptions<String, ChannelPostEvent> receiverOptions;
    private final Map<String, Sinks.Many<ChannelPostEvent>> topicSink = new HashMap<>();
    private final EventService eventService;

    @Autowired
    public ChannelPostMessageService(final KafkaSender<String, Object> kafkaSender,
                                     final ReceiverOptions<String, ChannelPostEvent> receiverOptions,
                                     final EventService eventService) {
        this.kafkaSender = kafkaSender;
        this.receiverOptions = receiverOptions;
        this.eventService = eventService;
    }

    public Mono<ChannelPostEvent> sendMessage(Topic topic, ChannelPostEvent channelPost) {
        log.info("Sending to topic={}, {}={}", topic, ChannelPost.class.getSimpleName(), channelPost);
        return kafkaSender
                .send(just(channelPost).map(post -> SenderRecord.create(new ProducerRecord<>(topic.toString(), post), post)))
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

    private Sinks.Many<ChannelPostEvent> consume(Topic topic) {
        var options = receiverOptions.subscription(Collections.singleton(topic.toString()))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        KafkaReceiver.create(options).receive()
                .doOnError(error -> log.error("Error receiving kafka events ", error))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofMinutes(1)))
                .doOnNext(record -> log.debug("Received kafka event: {}", record))
                .concatMap(record -> handleRecord(record, topic))
                .subscribe(record -> record.receiverOffset().acknowledge());
        return topicSink.get(topic.toString());
    }

    private static class HandleEvent implements BiConsumer<Tuple2<ReceiverRecord<String, ChannelPostEvent>, Optional<Iterable<Header>>>, SynchronousSink<ChannelPostEvent>> {
        @Override
        public void accept(Tuple2<ReceiverRecord<String, ChannelPostEvent>, Optional<Iterable<Header>>> tuple, SynchronousSink<ChannelPostEvent> sink) {
            if (tuple.getT2().isPresent() && tuple.getT1().value() == null) {
                for (Header header : tuple.getT2().get()) {
                    log.error("Deserialization error: {}", new String(header.value(), StandardCharsets.UTF_8));
                }
            } else {
                sink.next(tuple.getT1().value());
            }
        }
    }

    private Mono<Event<ChannelPost>> emitResults(Topic topic, ChannelPostEvent event) {
        Sinks.EmitResult emitResult = topicSink.get(topic.toString()).tryEmitNext(event);
        if (emitResult.isFailure()) {
            log.error("Failed to send event to topic Sink");
        }
        return just(event);
    }

    private Mono<ReceiverRecord<String, ChannelPostEvent>> handleRecord(
            ReceiverRecord<String, ChannelPostEvent> receiverRecord,
            Topic topic) {
        return just(receiverRecord)
                .map(this::extractDeserializationError)
                .handle(new HandleEvent())
                .flatMap(event -> emitResults(topic, event))
                .doOnError(error -> log.warn("Error processing event: key {}", receiverRecord, error))
                .onErrorResume(ex -> empty())
                .doOnNext(record -> log.debug("Successfully processed event: {}", record))
                .then(just(receiverRecord));
    }

    private Tuple2<ReceiverRecord<String, ChannelPostEvent>, Optional<Iterable<Header>>> extractDeserializationError(ReceiverRecord<String, ChannelPostEvent> receiverRecord) {
        return Tuples.of(receiverRecord, Optional.of(receiverRecord.headers().headers("springDeserializerExceptionValue")));
    }

    private Sinks.Many<ChannelPostEvent> getTopicSink(Topic topic) {
        var sink = topicSink.get(topic.toString());
        if (sink == null) {
            topicSink.put(topic.toString(), Sinks.many().multicast().onBackpressureBuffer());
            sink = consume(topic);
        }
        return sink;
    }

    private Flux<ChannelPostEvent> emitEventsAfterLastEventId(Topic topic, String lastEventId) {
        var topicSink = getTopicSink(topic);
        var combinedSink = Sinks.many().multicast().onBackpressureBuffer();
        var topicMessagePublisher = topicSink.asFlux()
                .doOnError(error -> log.error("Error while subscribing to topic sink", error))
                .doOnNext(combinedSink::tryEmitNext)
                .log();
        var lastEventsPublisher = eventService.eventsAfter(topic, lastEventId)
                .log()
                .doOnNext(combinedSink::tryEmitNext)
                .log();
        return Flux.merge(lastEventsPublisher, topicMessagePublisher);
    }

    public Flux<ChannelPostEvent> consumeMessage(Topic topic, String lastEventId) {
        var sink = getTopicSink(topic);
        return hasText(lastEventId) ? emitEventsAfterLastEventId(topic, lastEventId): sink.asFlux();
    }
}
