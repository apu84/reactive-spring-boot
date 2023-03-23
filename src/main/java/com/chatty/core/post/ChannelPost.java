package com.chatty.core.post;

public class ChannelPost extends Post {
    private String channelId;

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    public static ChannelPostBuilder builder() {
        return new ChannelPostBuilder();
    }
    public static class ChannelPostBuilder extends PostBuilder {
        private String channelId;
        public ChannelPostBuilder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }
        public ChannelPost build() {
            ChannelPost post = new ChannelPost();
            post.setChannelId(this.channelId);
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
