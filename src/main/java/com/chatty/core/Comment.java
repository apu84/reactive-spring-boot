package com.chatty.core;

import com.chatty.core.user.User;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

public class Comment {
    @Id
    private String id;
    private User user;
    private Comment parent;
    private List<Comment> replies;
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

    public String toString() {
        return String.format("Comment { id: %s, user: %s, content: %s, created: %s, lastModified: %d }",
                id, user.getName(), content, created, lastModified);
    }

}
