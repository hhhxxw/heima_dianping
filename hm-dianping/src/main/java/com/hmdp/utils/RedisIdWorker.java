package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author 一只咸鱼的大厂梦-hxw
 * @date 2025-08-10 09:30
 */
// Spring注解，让Spring帮我创建并管理这个类的实例，我以后可能要在其他地方用到它
@Component
public class RedisIdWorker {
    /**
     * 开始时间戳
     */
    // 一个自定义的“起始时间”
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    // 这是Spring提供的、用来和Redis数据库交互的便捷工具
    private final StringRedisTemplate stringRedisTemplate;
    // 定义了序列号在最终的64位ID中要占用多少个二进制位
    private static final int COUNT_BITS = 32;
    // 当Spring创建RedisIdWorker实例时，会自动把StringRedisTemplate这个工具通过这个构造函数传递进来。这是一种推荐的依赖注入方式
    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    // 我们对外提供的、用来获取新ID的核心方法。keyPrefix参数（比如"order"）可以区分不同业务的ID，让它们的计数器互不干扰
    public long nextId(String keyPrefix){
       // 1.生成时间戳
        // 代码获取当前服务器的本地时间 (LocalDateTime.now())
        LocalDateTime now = LocalDateTime.now();
        // 它将这个时间转换为从1970年开始计数的总秒数，并且是基于UTC（世界标准时间）。使用UTC至关重要，这能保证无论服务器在地球哪个角落，生成的时间戳都是一致的
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        // 用当前的秒数减去我们自定义的起始秒数（BEGIN_TIMESTAMP），得到一个相对较小的时间戳
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        // 2.生成序列号
        // 2.1获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2自增长
        // 请求Redis做一件它最擅长的事：对一个值进行原子性自增
        long count = stringRedisTemplate.opsForValue().increment("icr" + keyPrefix + ":" + date);

        // 3.拼接并返回
        // | count：这是一个按位或操作。它把左移后的结果和count进行合并。因为左移后，低32位全是0，所以这个操作能完美地把count的二进制数填充到低32位上
        return timestamp << COUNT_BITS | count;
        //最终的效果 [ 1位符号位 | 31位的时间戳 | 32位的序列号 ]
    }
}
