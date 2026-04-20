package com.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dto.Result;
import com.dto.UserDto;
import com.entity.Follow;
import com.mapper.FollowMapper;
import com.service.FollowService;
import com.service.UserService;
import com.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;
    @Override
    public Result follow(Long followUserId, Boolean isFollow){
        Long userid = UserHolder.getUser().getId();
        String key = "follows:"+userid;
        if(isFollow){
            Follow follow = new Follow();
            follow.setUserId(userid);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if(isSuccess){

                stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
        }else{
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userid)
                    .eq("follow_user_id", followUserId));
            if(isSuccess){
                stringRedisTemplate.opsForSet().remove(key,followUserId.toString());
            }

        }


        return Result.ok();
    }

    @Override
    public Result follow(Long followUserId){
        Long userid = UserHolder.getUser().getId();

        Long count = query().eq("user_id", userid)
                .eq("follow_user_id", followUserId).count();


        return Result.ok(count>0);
    }
    @Override
    public Result common(Long id){
        Long userId = UserHolder.getUser().getId();
        String key = "follows:"+userId;
        String key2= "follows:"+id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if(intersect==null||intersect.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDto> userDTOS = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDto.class))
                .collect(Collectors.toList());

        return Result.ok(userDTOS);
    }





}