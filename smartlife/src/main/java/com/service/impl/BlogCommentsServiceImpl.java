package com.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.BlogComments;
import com.mapper.BlogCommentsMapper;
import com.service.BlogCommentsService;
import org.springframework.stereotype.Service;


@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper,BlogComments> implements  BlogCommentsService {

}
