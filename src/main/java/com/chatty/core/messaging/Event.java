package com.chatty.core.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@SuperBuilder()
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Event<T> {
    @Id
    String id;
    EventType eventType;
    String event;
    @Field("data")
    T data;
    Date dateTime;

    public enum EventType {
        CREATED,
        UPDATED,
        REMOVED,
        KEEP_ALIVE,
    }
}
