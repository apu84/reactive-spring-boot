package com.chatty.core.messaging;

import com.chatty.core.channel.ChannelService;
import com.chatty.core.post.ChannelPost;
import com.chatty.core.space.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Mono.zip;
@Slf4j
@Service
public class ChannelPostMessageService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SpaceService spaceService;
    private final ChannelService channelService;
//    private final ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate;
    private final KafkaSender<String, Object> kafkaSender;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

    @Autowired
    public ChannelPostMessageService(final KafkaTemplate<String, Object> kafkaTemplate,
//                                     final ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate,
                                     final KafkaSender<String, Object> kafkaSender,
                                     final SpaceService spaceService,
                                     @Lazy final ChannelService channelService) {
        this.kafkaTemplate = kafkaTemplate;
        this.spaceService = spaceService;
        this.channelService = channelService;
//        this.reactiveKafkaProducerTemplate = reactiveKafkaProducerTemplate;
        this.kafkaSender = kafkaSender;
    }
    public Mono<SenderResult<ChannelPost>> sendMessage(ChannelPost post) {
         return buildTopic(post).flatMap(topic -> sendReactiveMessage(topic, post));
    }

    private Mono<Topic> buildTopic(ChannelPost channelPost) {
        return channelService
                .get(channelPost.getChannelId())
                .flatMap(channel -> zip(just(channel), spaceService.get(channel.getSpaceId())))
                .map(tuple -> Topic.builder()
                        .spaceName(tuple.getT2().getName())
                        .channelName(tuple.getT1().getName())
                        .build());
    }
    private Mono<SendResult<String, Object>> sendMessage(Topic topic, ChannelPost channelPost) {
        log.info("Send to topic={}, {}={}", topic, ChannelPost.class.getSimpleName(), channelPost);
        return  Mono.fromFuture(kafkaTemplate.send(topic.toString(), channelPost));
    }

    private Mono<SenderResult<ChannelPost>> sendReactiveMessage(Topic topic, ChannelPost channelPost) {
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
}
