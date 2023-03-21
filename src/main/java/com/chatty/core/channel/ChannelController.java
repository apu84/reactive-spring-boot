package com.chatty.core.channel;

import com.chatty.core.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.chatty.core.channel.Channel.fromChannel;

@RequestMapping("/channel")
@RestController
public class ChannelController {
    private ChannelRepository channelRepository;
    @Autowired
    public ChannelController(final ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }
    @PostMapping
    public Mono<Channel> createChannel(@RequestBody Mono<Channel> channelMono) {
        return channelMono.flatMap(channel -> channelRepository.save(channel));
    }
    @GetMapping("/{id}")
    public Mono<Channel> getChannel(@PathVariable String channelId) {
        return channelRepository.findById(channelId);
    }
    @GetMapping
    public Flux<Channel> getAllChannels() {
        return channelRepository.findAll();
    }
    @PutMapping("/{id}")
    public Mono<Channel> updateChannel(@PathVariable String id,
                                       @RequestBody Mono<Channel> updateChannel) {
        return Mono.just(id)
                .flatMap(chId -> channelRepository.findById(id))
                .switchIfEmpty(Mono.error(new BadRequestException("Invalid channelId")))
                .flatMap(channel -> {
                    return updateChannel.flatMap(ch -> {
                        Channel updated = fromChannel(channel)
                                .name(ch.getName())
                                .label(ch.getLabel())
                                .topics(ch.getTopics())
                                .build();
                        return channelRepository.save(updated);
                    });
                });
    }
}
