package com.chatty.core.channel;

import com.chatty.core.post.Post;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class Channel {
    @Id
    String id;
    String name;
    String label;
    List<String> userIds;
    List<Post> posts = new ArrayList<>();

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

    public List<Post> getPosts() {
        return posts;
    }
}
