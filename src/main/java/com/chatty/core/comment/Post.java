package com.chatty.core.comment;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    @Id
    private String id;
    private String userId;
    private Post parent;
    private List<String> replyIds;
    private String content = "";
    Date created;
    Date lastModified;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
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

    public void setUserId(String userId) {
        this.userId = userId;
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
        return String.format("Post { id: %s, userId: %s, content: %s, created: %s, lastModified: %d }", id, userId, content, created, lastModified);
    }
}
