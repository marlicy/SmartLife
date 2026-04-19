package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dto.Result;
import com.entity.Shop;

public interface ShopService extends IService<Shop> {

    Result queryShopById(Long id);

    Shop queryShopCache(Long id);

    Result update(Shop shop);
}
