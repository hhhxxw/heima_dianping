package com.hmdp.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    // 1. 注入StringRedisTemplate，专门用来操作String类型的key和value
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        // 2. 定义缓存Key
        String key = CACHE_SHOP_TYPE_KEY;

        // 3. 从Redis查询商铺类型缓存 (此时得到的是JSON字符串)
        String shopTypeJson = stringRedisTemplate.opsForValue().get(key);

        // 4. 判断缓存是否命中
        if (StrUtil.isNotBlank(shopTypeJson)) {
            // 4.1. 缓存命中，直接将JSON字符串反序列化为List<ShopType>并返回
            // Hutool工具包可以很方便地处理JSON
            List<ShopType> typeList = JSONUtil.toList(shopTypeJson, ShopType.class);
            return Result.ok(typeList);
        }

        // 5. 缓存未命中，查询数据库
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();

        // 6. 判断数据库中是否存在数据
        if (typeList == null || typeList.isEmpty()) {
            // 数据库中也不存在，可以直接返回错误或空集合
            // 也可以缓存一个空值，防止“缓存穿透”，但这里为了简单先直接返回
            return Result.fail("店铺分类信息不存在！");
        }

        // 7. 数据库中存在，将查询到的数据序列化为JSON字符串
        String json = JSONUtil.toJsonStr(typeList);


        // 8. 将数据写入Redis，并设置过期时间（例如30分钟）
        stringRedisTemplate.opsForValue().set(key, json, CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);

        // 9. 返回从数据库查询到的数据
        return Result.ok(typeList);
    }
}
