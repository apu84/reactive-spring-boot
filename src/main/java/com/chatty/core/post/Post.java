package com.chatty.core.post;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Post {
    @Id
    private String id;
    @JsonView(PostViews.Internal.class)
    private String senderId;
    private String parentId;
    private Post parent;
    @Transient
    private List<String> replyIds;
    @JsonView(PostViews.Public.class)
    private String content = "";
    @JsonView(PostViews.Public.class)
    Date created;
    @JsonView(PostViews.Public.class)
    Date lastModified;
    @JsonView(PostViews.Public.class)
    private boolean hasReplies;

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public Post getParent() {
        return parent;
    }

    private void setReplyIds(List<String> replyIds) {
        this.replyIds = replyIds;
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

    private void setContent(String content) {
        this.content = content;
        this.lastModified = new Date();
    }

    private void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    private void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    private void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean hasReplies() {
        return hasReplies;
    }

    private void setHasReplies(boolean hasReplies) {
        this.hasReplies = hasReplies;
    }

    private void setId(String id) {
        this.id = id;
    }

    public Post() {
        this.created = new Date();
    }

    public String toString() {
        return String.format("Post { id: %s, userId: %s, content: %s, created: %s, lastModified: %s }", id, senderId, content, created, lastModified);
    }

    public static PostBuilder fromPost(Post post) {
        return PostBuilder.builder()
                .id(post.getId())
                .parentId(post.parentId)
                .senderId(post.senderId)
                .content(post.content)
                .replyIds(post.replyIds);
    }

    static class PostBuilder {
        private String id;
        private String senderId;
        private String parentId;
        private String content;
        private List<String> replyIds;
        private Date created;
        private Date lastModified;
        private boolean hasReplies;

        public static PostBuilder builder() {
            return new PostBuilder();
        }
        public PostBuilder senderId(String senderId) {
            this.senderId = senderId;
            return this;
        }
        public PostBuilder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }
        public PostBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PostBuilder replyIds(List<String> replyIds) {
            this.replyIds = replyIds;
            return this;
        }
        public PostBuilder created(Date created) {
            this.created = created;
            return this;
        }
        public PostBuilder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }
        public PostBuilder hasReplies(boolean hasReplies) {
            this.hasReplies = hasReplies;
            return this;
        }
        public PostBuilder id(String id) {
            this.id = id;
            return this;
        }
        public Post build() {
            Post post = new Post();
            post.setSenderId(this.senderId);
            post.setParentId(this.parentId);
            post.setContent(this.content);
            if (this.replyIds != null) {
                post.setReplyIds(this.replyIds);
            }
            if (this.created != null) {
                post.setCreated(this.created);
            }
            if (this.lastModified != null) {
                post.setLastModified(this.lastModified);
            }
            post.setHasReplies(this.hasReplies);
            if(this.id != null) {
                post.setId(this.id);
            }
            return post;
        }
    }
}
