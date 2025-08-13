package com.hmdp.utils;

/**
 * @author 一只咸鱼的大厂梦-hxw
 * @date 2025-08-13 16:45
 */
public interface ILock {
    /**
     * 舱室获取锁
     * @param timeoutSec 锁持有的超时时间， 过期后自动释放
     * @return true 表示锁成功， false 代码获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
