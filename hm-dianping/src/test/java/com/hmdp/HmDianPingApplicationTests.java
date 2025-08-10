package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

@SpringBootTest
class HmDianPingApplicationTests {
    @Resource
    private CacheClient cacheClient;
    @Resource
    private ShopServiceImpl shopService;
    @Autowired
    private RedisIdWorker redisIdWorker;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    // 模拟了一次高并发的压力场景，用来验证我们的RedisIdWorker不仅能正确生成不重复的ID，而且性能也足够好
    //  Junit测试框架的注解，标记这个方法是一个可以独立运行的测试单元
    @Test
    void testIdWorker() throws InterruptedException {
        // 这是一个同步工具，可以把它想象成一个门。这里我们设置了需要300个“信号”才能开门。
        CountDownLatch latch = new CountDownLatch(300);
        // 定义了每个线程需要做的工作。在这里，每个线程的任务是循环100次，每次都向redisIdWorker申请一个用于“order”业务的ID
        Runnable task = () -> {
            for(int i = 0; i < 100; i ++){
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            // 每个线程在完成了自己的100次ID申请后，就会调用这个方法，相当于发出了一个“我完成了”的信号，让门闩的计数器减1
            latch.countDown();
        };

        long begin = System.currentTimeMillis();
        // 这里循环300次，把300个任务提交给一个线程池去执行。这模拟了300个用户在同时请求ID
        for(int i = 0; i < 300; i ++){
            es.submit(task);
        }
        //  主线程执行到这里会暂停，它会一直等待，直到门闩的计数器被减到0（也就是所有300个任务都完成了
        latch.await();
        long end = System.currentTimeMillis();
        // 一旦所有任务完成，主线程被唤醒，它会计算并打印出完成这300个任务（总计生成 300 * 100 = 30000 个ID）所花费的总时间
        System.out.println("time = " + (end - begin));
    }

    @Test
    void testSaveShop() throws InterruptedException {
//        shopService.saveShop2Redis(1L, 10L);
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY, shop, 10L, TimeUnit.SECONDS);
    }

}
