package com.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dto.Result;
import com.dto.ScrollResult;
import com.dto.UserDto;
import com.entity.Blog;
import com.entity.Follow;
import com.entity.User;
import com.mapper.BlogMapper;
import com.service.BlogService;
import com.service.FollowService;
import com.service.UserService;
import com.utils.SystemConstants;
import com.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static com.utils.RedisConstants.*;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private FollowService followService;


    @Override
    public Result queryHotBlog(Integer current){
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    @Override
    public Result queryBlogById(Long id){
        //查询blog
        Blog blog = getById(id);
        if(blog==null){
            return Result.fail("笔记不存在");
        }
        //查询有关blog的用户
        queryBlogUser(blog);
        isBlogLiked(blog);
        return null;
    }

    private void isBlogLiked(Blog blog) {
        UserDto user = UserHolder.getUser();
        if(user==null){
            return;
        }
        Long userId = user.getId();
        String key = BLOG_LIKED_KEY+blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score!=null);
    }


    @Override
    public Result likeBlog(Long id){
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY+id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if(score==null){
            boolean isSuccess = update().setSql("liked=liked+1").eq("id", id).update();
            if(isSuccess){
                stringRedisTemplate.opsForZSet().add(key, userId.toString(),System.currentTimeMillis());
            }
        }else{
            boolean isSuccess = update().setSql("liked=liked-1").eq("id", id).update();
            if(isSuccess){
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());

            }


        }
        return Result.ok();

    }

    @Override
    public Result queryBlogLikes(Long id){
        String key = BLOG_LIKED_KEY+id;
        //1.查询top5点赞用户
        Set<String> top5Id = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        //2.解析出其中用户id
        List<Long> ids = top5Id.stream().map(Long::valueOf).collect(Collectors.toList());
        if(top5Id==null||top5Id.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        //3.根据用户id查询用户
        String idStr = StrUtil.join(",", ids);
        List<UserDto> userDTOS = userService.query()
                .in("id",ids).last("ORDER BY FIELDS(id,"+idStr+")").list()
                .stream().map(user -> BeanUtil.copyProperties(user, UserDto.class))
                .collect(Collectors.toList());
        //4.返回

        return Result.ok(userDTOS);
    }

    @Override
    public Result saveBlog(Blog blog){
        UserDto user = UserHolder.getUser();
        blog.setUserId(user.getId());
        boolean isSuccess = save(blog);
        if(!isSuccess){

            return Result.fail("新增笔记失败");
        }
        //查询粉丝
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        //推送笔记id给粉丝
        for (Follow follow : follows) {
            Long userId = follow.getUserId();
            String key = FEED_KEY+userId;
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }

        return Result.ok(blog.getId());
    }



    @Override
    public Result queryBlogFollow(Long max,Integer offset){
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();

        //2.查询收件箱
        String key = FEED_KEY+userId;

        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 3);
        //非空判断
        if(typedTuples==null||typedTuples.isEmpty()){
            return Result.ok();

        }
        //3.解析数据：blogId，minTime（时间戳），offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os=1;
        for (ZSetOperations.TypedTuple<String> type : typedTuples) {
            ids.add(Long.valueOf(type.getValue()));

            long time = type.getScore().longValue();
            if(time==minTime){
                os++;
            }else{
                minTime=time;
                os=1;
            }
        }
        //4.根据id查询blog
        String idStr = StrUtil.join(",", ids);

        List<Blog> blogs =query()
                .in("id",ids).last("ORDER BY FIELDS(id,"+idStr+")").list();
        for (Blog blog : blogs) {
            //查询blog有关的用户
            queryBlogUser(blog);
            //查询blog是否被点赞
            isBlogLiked(blog);
        }
        //5.封装并返回
        ScrollResult s = new ScrollResult();
        s.setList(blogs);
        s.setOffset(os);
        s.setMinTime(minTime);
        return Result.ok(s);
    }










    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

}
