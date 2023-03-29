package com.chatty.core.post;

import com.chatty.core.CrudService;
import org.springframework.stereotype.Component;

@Component
public class PostService extends CrudService<Post, PostRepository, String> {
    public PostService(PostRepository repository) {
        super(repository);
    }
}

