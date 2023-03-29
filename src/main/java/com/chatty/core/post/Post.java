package com.chatty.core.post;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.Date;
import java.util.List;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    private String id;
    @JsonView(PostViews.Internal.class)
    private String senderId;
    private String parentId;
    @Transient
    private List<String> replyIds;
    @JsonView(PostViews.Public.class)
    @Builder.Default
    private String content = "";
    @JsonView(PostViews.Public.class)
    @Builder.Default
    Date created = new Date();
    @JsonView(PostViews.Public.class)
    @Builder.Default
    Date lastModified = new Date();
    @JsonView(PostViews.Public.class)
    boolean repliesPresent;
}
