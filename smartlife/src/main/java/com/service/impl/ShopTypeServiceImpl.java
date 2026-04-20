package com.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entity.ShopType;
import com.mapper.ShopTypeMapper;
import com.service.ShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public List<ShopType> queryTypeList(){
        String key = "login:type";
        String shopTypeJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(shopTypeJson)){
            return  JSONUtil.toList(shopTypeJson, ShopType.class);

        }
        List<ShopType> typeList = this.query().orderByAsc("sort").list();
        if(typeList.isEmpty()){
            return new ArrayList<>();
        }
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(typeList));


        return typeList;

    }


}
