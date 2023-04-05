package com.chatty.core.messaging;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Builder()
@Getter
public class Event<T> {
    @Id
    String id;
    EventType eventType;
    String event;
    T data;
    Date dateTime;
    public enum EventType {
        CREATED,
        UPDATED,
        REMOVED,
    }
}
