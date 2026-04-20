package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dto.Result;
import com.entity.Blog;

public interface BlogService extends IService<Blog> {

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogFollow(Long max, Integer offset);
}