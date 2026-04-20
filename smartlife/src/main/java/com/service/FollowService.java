package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dto.Result;
import com.entity.Follow;

public interface FollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result follow(Long followUserId);

    Result common(Long id);
}