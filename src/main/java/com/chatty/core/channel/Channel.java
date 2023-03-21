package com.chatty.core.channel;

import com.chatty.core.post.Post;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Channel {
    @Id
    String id;
    String name;
    String label;
    String topics;
    List<String> userIds;
    List<String> postIds;
    Date created;
    Date lastModified;

    private Channel() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public List<String> getPostIds() {
        return postIds;
    }

    public String getTopics() {
        return topics;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public static ChannelBuilder fromChannel(Channel channel) {
        return ChannelBuilder.builder()
                .id(channel.getId())
                .name(channel.getName())
                .label(channel.getLabel())
                .topics(channel.getTopics())
                .userIds(channel.getUserIds())
                .postIds(channel.getPostIds());
    }

    static class ChannelBuilder {
        String id;
        String name;
        String label;
        String topics;
        List<String> userIds;
        List<String> postIds;
        Date created;
        Date lastModified;

        private ChannelBuilder() {
        }

        public static ChannelBuilder builder() {
            return new ChannelBuilder();
        }

        public ChannelBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ChannelBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChannelBuilder label(String label) {
            this.label = label;
            return this;
        }

        public ChannelBuilder topics(String topics) {
            this.topics = topics;
            return this;
        }

        public ChannelBuilder created(Date created) {
            this.created = created;
            return this;
        }

        public ChannelBuilder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public ChannelBuilder userIds(List<String> userIds) {
            this.userIds = userIds;
            return this;
        }

        public ChannelBuilder postIds(List<String> postIds) {
            this.postIds = postIds;
            return this;
        }

        public Channel build() {
            Channel channel = new Channel();
            channel.id = this.id;
            channel.name = this.name;
            channel.label = this.label;
            channel.topics = this.topics;
            channel.created = this.created == null ? new Date() : this.created;
            channel.lastModified = this.lastModified == null ? new Date() : this.lastModified;
            channel.userIds = this.userIds == null ? new ArrayList<>() : this.userIds;
            channel.postIds = this.postIds == null ? new ArrayList<>() : this.postIds;
            return channel;
        }

    }
}
