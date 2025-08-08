package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {
    // 逻辑过期时间
    private LocalDateTime expireTime;
    // 希望存储到redis的数据
    private Object data;
}
