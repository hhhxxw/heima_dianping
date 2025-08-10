# ⭐ 黑马点评 (Heima-Dianping)

[![License: MIT](D:\code\redis_study\heima_dianping\Redis实战篇.assets\License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](D:\code\redis_study\heima_dianping\Redis实战篇.assets\Java-1.8+-orange.svg)](https://www.java.com)
[![Spring Boot](D:\code\redis_study\heima_dianping\Redis实战篇.assets\Spring Boot-2.7.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](D:\code\redis_study\heima_dianping\Redis实战篇.assets\Vue.js-2.x-green.svg)](https://vuejs.org/)
[![Redis](D:\code\redis_study\heima_dianping\Redis实战篇.assets\Redis-6.x-red.svg)](https://redis.io/)

一份“黑马点评”项目的完整实现，这是一个仿照“大众点评”的业务场景，旨在通过实战深入学习和应用Java后端及分布式开发的各项核心技术。该项目覆盖了从基础功能到高并发、分布式系统设计的全过程。

---

## ✨ 项目特色

* **全栈实践**: 项目包含后端API服务和前端用户界面，提供一个完整的Web应用体验。
* **主流技术栈**: 采用业界广泛应用的Spring Boot、MyBatis-Plus、Vue.js等技术。
* **高并发场景实战**: 针对秒杀优惠券、关注/取关等高并发场景，提供了多种分布式解决方案。
* **Redis深度应用**: 不仅仅是作为缓存，更深入地运用Redis实现分布式锁、Feed流、UV统计等高级功能。
* **代码规范**: 代码结构清晰，遵循阿里巴巴Java开发规约，易于理解和二次开发。

---

## 📸 项目截图

*(建议在此处替换为你自己项目的截图)*

|                   首页                    |                    店铺详情                     |                    秒杀下单                    |
| :---------------------------------------: | :---------------------------------------------: | :--------------------------------------------: |
| ![首页](path/to/your/screenshot_home.png) | ![店铺详情](path/to/your/screenshot_detail.png) | ![秒杀下单](path/to/your/screenshot_order.png) |

---

## 🛠️ 技术栈

| 分类          | 技术              | 描述                         |
| :------------ | :---------------- | :--------------------------- |
| **后端**      | Spring Boot       | 核心依赖管理与自动化配置框架 |
|               | Spring MVC        | Web框架，提供HTTP接口        |
|               | MyBatis-Plus      | ORM框架，简化数据库操作      |
|               | Spring Data Redis | 简化Redis操作的工具集        |
|               | Hutool            | Java工具类库，简化开发       |
|               | Lombok            | 简化JavaBean开发的工具       |
| **前端**      | Vue.js            | 渐进式JavaScript框架         |
|               | Element UI        | 基于Vue的桌面端UI组件库      |
|               | Axios             | 基于Promise的HTTP客户端      |
| **数据库**    | MySQL 8.0         | 关系型数据库                 |
|               | Redis             | 高性能的Key-Value内存数据库  |
| **中间件**    | Nginx             | 高性能HTTP和反向代理服务器   |
| **构建/依赖** | Maven             | 项目构建和依赖管理工具       |
| **测试**      | JUnit             | Java单元测试框架             |


---

## 🚀 快速开始

请确保你的开发环境已安装以下软件：

* JDK 1.8+
* Maven 3.6+
* Node.js v14+
* MySQL 8.0+
* Redis 6.x+
* Nginx (可选，用于反向代理)

### 1. 克隆项目

```bash
git clone [https://github.com/your-username/heima-dianping.git](https://github.com/your-username/heima-dianping.git)
cd heima-dianping
```

### 2. 后端启动

1.  **数据库初始化**:
    * 创建一个名为 `hmdp` 的数据库。
    * 将项目中的 `hmdp.sql` 文件导入到该数据库中。

2.  **修改配置**:
    * 打开 `src/main/resources/application.yml`。
    * 修改 `spring.datasource` 下的数据库连接信息 (URL, username, password)。
    * 修改 `spring.redis` 下的Redis连接信息 (host, port, password)。

3.  **启动服务**:
    * 使用IDE（如IntelliJ IDEA）打开项目，它会自动识别为Maven项目。
    * 找到 `HmDianPingApplication.java` 并运行它的 `main` 方法。
    * 服务默认启动在 `http://localhost:8081`。

### 3. 前端启动

1.  **进入前端目录**:
    ```bash
    cd frontend/hm-dianping
    ```
2.  **安装依赖**:
    ```bash
    npm install
    ```
3.  **启动服务**:
    ```bash
    npm run serve
    ```
4.  **访问**:
    * 前端服务默认启动在 `http://localhost:8080`。
    * 在浏览器中打开此地址即可访问项目。

### 4. Nginx配置 (可选)

为了解决前端跨域问题，推荐使用Nginx进行反向代理。

```nginx
server {
    listen       80; # 前端访问端口
    server_name  hmdp.com; # 可在hosts文件中配置

    location / {
        root   /path/to/your/frontend/dist; # 前端打包后的dist目录
        index  index.html;
    }

    # API接口反向代理
    location /api {
        proxy_pass http://localhost:8081; # 后端服务地址
    }
}
```

---

## 🎯 核心功能与实现逻辑

本项目不仅仅是业务代码的堆砌，更包含了对后端高频问题的解决方案。

1.  **短信登录与分布式Session**:
    * **逻辑**: 使用Redis存储验证码，并替代传统Session。用户登录成功后生成Token，将用户信息存入Redis的Hash结构中，实现分布式Session共享。
    * **技术**: `Redis (String/Hash)`, `JWT` 或自定义Token方案。

2.  **优惠券秒杀 (高并发)**:
    * **挑战**: 超卖、一人一单问题。
    * **解决方案**:
        * **乐观锁**: 通过版本号或CAS机制解决并发更新的冲突。
        * **一人一单**: 利用Redis的`setnx`或数据库的唯一索引保证用户只能下一单。
        * **分布式锁**: 采用基于Redis的`SETNX`或Redisson框架实现分布式锁，确保扣减库存操作的原子性。
        * **异步秒杀**: 利用Redis的消息队列或阻塞队列，将下单请求削峰填谷，由后台线程异步处理，提升用户体验和系统吞吐量。

3.  **达人探店与Feed流**:
    * **逻辑**: 实现类似微博/朋友圈的关注动态流。
    * **解决方案 (推模式)**: 当达人发布探店笔记后，主动将笔记ID推送给所有粉丝。每个粉丝有一个收件箱（Mailbox），使用Redis的`SortedSet`实现，可根据时间戳排序。
    * **技术**: `Redis (SortedSet)`。

4.  **附近商户 (GEO)**:
    * **逻辑**: 根据用户地理位置，推送附近的商户。
    * **解决方案**: 将商户的地理坐标（经纬度）存入Redis的`GEO`数据结构中，利用`GEORADIUS`或`GEOSEARCH`命令高效查询指定范围内的商户。
    * **技术**: `Redis (GEO)`。

5.  **UV统计 (日活/月活)**:
    * **挑战**: 海量数据下的精确去重非常消耗内存。
    * **解决方案**: 使用Redis的`HyperLogLog`数据结构，它能在极小的内存消耗下，对海量数据进行近似去重统计，误差率极低。
    * **技术**: `Redis (HyperLogLog)`。

---

## 📜 项目结构

```
heima-dianping
├── frontend/             -- 前端Vue项目源码
├── sql/                  -- 数据库初始化脚本
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hmdp/
│   │   │       ├── HmDianPingApplication.java  -- Spring Boot 启动类
│   │   │       ├── config/                     -- 配置类 (MVC, MybatisPlus等)
│   │   │       ├── controller/                 -- 控制层 (API接口)
│   │   │       ├── dto/                        -- 数据传输对象 (Data Transfer Object)
│   │   │       ├── entity/                     -- 数据库实体类
│   │   │       ├── mapper/                     -- MyBatis-Plus Mapper接口
│   │   │       ├── service/                    -- 业务逻辑层
│   │   │       ├── utils/                      -- 工具类 (RedisIdWorker, CacheClient等)
│   │   └── resources/
│   │       ├── mapper/                         -- Mybatis XML映射文件
│   │       ├── application.yml                 -- Spring Boot 核心配置文件
│   └── test/                 -- 单元测试
├── pom.xml                 -- Maven依赖配置文件
└── README.md               -- 就是本文件
```

---

## 🙏 致谢

* **黑马程序员**: 感谢其提供了如此优秀的教学项目，本项目完全基于其课程内容进行实现和学习。
* 所有为本项目贡献代码的开源社区和开发者。

---
## 📄 许可证

本项目采用 [MIT](https://opensource.org/licenses/MIT) 许可证。