package com.chatty.core.space;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Getter
public class Space {
    @Id
    private String id;
    @NonNull
    private String name;
    @NonNull
    private String label;
    @Builder.Default
    private Date created = new Date();
    @Builder.Default
    private Date lastModified = new Date();
}
