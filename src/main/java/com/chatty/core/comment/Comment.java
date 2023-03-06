package com.chatty.core.comment;

import com.chatty.core.user.User;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {
    @Id
    private String id;
    private User user;
    private Comment parent;
    private List<String> replyIds;
    private String content = "";
    Date created;
    Date lastModified;

    public Comment(final User user, final String content) {
        this(user, content, null);
    }

    public Comment(final User user, final String content, final Comment parent) {
        this.user = user;
        this.content = content;
        this.parent = parent;
        this.created = new Date();
        this.lastModified = new Date();
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Comment getParent() {
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
    }

    public List<String> addReply(final String commentId) {
        if (replyIds == null) {
            replyIds = new ArrayList<>();
        }
        replyIds.add(commentId);
        return replyIds;
    }

    public String toString() {
        return String.format("Comment { id: %s, user: %s, content: %s, created: %s, lastModified: %d }", id, user.getName(), content, created, lastModified);
    }
}
