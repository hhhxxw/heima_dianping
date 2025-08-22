-- seckill.lua

-- 1. 参数列表
-- 优惠券ID
local voucherId = ARGV[1]
-- 用户ID
local userId = ARGV[2]

local orderId = ARGV[3]

-- 2. 数据Key
-- 库存Key
local stockKey = 'seckill:stock:' .. voucherId
-- 订单Key (用于一人一单判断, 使用Set)
local orderKey = 'seckill:order:' .. voucherId

-- 3. 脚本业务
-- 3.1. 判断库存是否充足
if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 库存不足，返回1
    return 1
end

-- 3.2. 判断用户是否已下单 (SISMEMBER)
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 已经下过单，返回2
    return 2
end

-- 3.3. 扣减库存 (decr)
redis.call('decr', stockKey)
-- 3.4. 将用户ID存入Set集合 (sadd)
redis.call('sadd', orderKey, userId)

-- 3.5 发送消息到队列当中
redis.call('xadd', 'stream.orders', '*', 'userId',userId, 'voucherId', voucherId, 'id', orderId)

-- 下单成功，返回0
return 0