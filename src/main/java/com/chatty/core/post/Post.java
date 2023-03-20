package com.chatty.core.post;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    @Id
    private String id;
    @JsonView(PostViews.Internal.class)
    private String senderId;
    private Post parent;
    private List<String> replyIds;
    @JsonView(PostViews.Public.class)
    private String content = "";
    @JsonView(PostViews.Public.class)
    Date created;
    @JsonView(PostViews.Public.class)
    Date lastModified;

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public Post getParent() {
        return parent;
    }

    public List<String> getReplyIds() {
        return replyIds;
    }

    public String getContent() {
        return content;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setContent(String content) {
        this.content = content;
        this.lastModified = new Date();
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public List<String> addReply(final String commentId) {
        if (replyIds == null) {
            replyIds = new ArrayList<>();
        }
        replyIds.add(commentId);
        return replyIds;
    }

    public Post() {
        this.created = new Date();
    }

    public String toString() {
        return String.format("Post { id: %s, userId: %s, content: %s, created: %s, lastModified: %s }", id, senderId, content, created, lastModified);
    }
}
