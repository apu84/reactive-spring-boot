package com.chatty.core.messaging;

import lombok.Builder;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Builder()
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
