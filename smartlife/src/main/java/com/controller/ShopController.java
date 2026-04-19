package com.controller;

import com.dto.Result;
import com.entity.Shop;
import com.service.ShopService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {


    @Resource
    public ShopService shopService;

    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id){
        return shopService.queryShopById(id);
    }
    @PutMapping
    public Result updateShop(@RequestBody Shop shop){
        return shopService.update(shop);
    }





}
