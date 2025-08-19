package com.hmdp.service.impl;

import com.hmdp.config.RedissonConfig;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    // 利用seckillVoucherService根据id进行查询
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 提前加载Lua脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        // 设置脚本的位置，需要在resources目录下创建一个unlock.lua文件
        SECKILL_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        // 设置脚本的返回值类型
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    // 版本一

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        // 1. 查询优惠卷信息
//        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
//        // 2. 判断秒杀是否开始
//        if(voucher.getBeginTime().isAfter(LocalDateTime.now())){
//            return Result.fail("秒杀尚未开始");
//        }
//        // 3. 判断秒杀是否结束
//        if(voucher.getEndTime().isBefore(LocalDateTime.now())){
//            return Result.fail("秒杀已经结束");
//        }
//        // 4. 判断库存是否充足
//        if(voucher.getStock() < 1){
//            return Result.fail("库存不足");
//        }
//        Long userId = UserHolder.getUser().getId();
//        synchronized (userId.toString().intern()) {
//            // 创建对象
//            // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
//
//            RLock lock = redissonClient.getLock("lock:order:" + userId);
//            // 获取锁
//            boolean isLock = lock.tryLock();
//            if(!isLock){
//                // 获取锁失败，返回错误信息或重试
//                return Result.fail("不允许重复下下单");
//            }
//            try {
//                // 获取代理对象（事务）
//                IVoucherOrderService proxy = (IVoucherOrderService)AopContext.currentProxy();
//                return proxy.createVoucherOrder(voucherId);
//            } catch (IllegalStateException e) {
//                throw new RuntimeException(e);
//            }finally {
//                // 释放锁
//                lock.unlock();
//            }
//        }
//    }

    // 版本二
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 获取用户
        Long userId = UserHolder.getUser().getId();
        // 加载lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );
        int r = result.intValue();
        if(r != 0){
            return  Result.fail(r == 1? "库存不足" : "不能重复下单");
        }
        long orderId = redisIdWorker.nextId("order");

        // TODO 保存阻塞队列

        // 返回订单id
        return Result.ok(orderId);
    }
    @Transactional
    public  Result createVoucherOrder(Long voucherId) {
        // 5. 一人一单
        Long userId = UserHolder.getUser().getId();


            // 5.1 查询订单
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();

            // 5.2 判断是否存在
            if(count > 0){
                // 用户已经购买过了
                return Result.fail("用户已经购买过一次了");
            }
            // 6. 扣减库存
            boolean success = seckillVoucherService.update()
                    // setSql方法可以自定义SQL语句，实现 "SET stock = stock - 1"
                    .setSql("stock = stock - 1")
                    // eq方法是添加WHERE条件，即 "voucher_id = ?"
                    .eq("voucher_id", voucherId)
                    // gt方法也是添加WHERE条件，即 "AND stock > ?"
                    .gt("stock", 0)
                    .update();
            if(!success){
                // 扣减失败
                return Result.fail("库存不足");
            }
            // 7. 创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            // 7.1 订单ID
            long orderId = redisIdWorker.nextId("order");
            voucherOrder.setId(orderId);
            voucherOrder.setUserId(userId);
            // 7.3 代金卷ID
            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            // 7. 返回订单ID
            return Result.ok(orderId);
    }

}
