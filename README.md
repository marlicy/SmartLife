# Smart Life

`Smart Life` 是一个基于 Spring Boot 的综合性生活服务后端项目，提供用户、商铺、博客、关注、秒杀、优惠券、聊天与 AI 工具等能力。项目中集成了 Redis 缓存、消息队列、分布式锁、限流、MyBatis 持久化等常见中间件实践，适合作为学习和二次开发的基础工程。

## 项目亮点

- 用户登录、信息管理与拦截器鉴权
- 商铺、商铺类型与地图/列表类查询
- 博客发布、评论、关注与信息流相关功能
- 优惠券与秒杀下单流程
- Redis 缓存、缓存重建与热点数据优化
- 分布式锁、ID 生成与限流控制
- AI 聊天、AI 工具路由与业务工具集成
- 消息生产/消费相关实现

## 技术栈

- Java
- Spring Boot
- MyBatis / MyBatis-Plus 风格数据访问
- Redis
- Kafka
- Lua 脚本
- Maven

## 目录说明

- `smartlife/src/main/java`：核心业务代码，包含 `controller`、`service`、`mapper`、`entity`、`config`、`interceptor`、`messaging` 等模块
- `smartlife/src/main/resources`：配置文件、静态资源、Mapper XML、Lua 脚本和初始化 SQL
- `smartlife/docs`：项目说明与测试相关文档
- `smartlife/src/test`：测试代码

## 主要功能模块

### 1. 用户与认证

- 用户注册/登录
- 登录态维护
- 用户信息读取与更新
- 请求拦截与权限控制

### 2. 商铺与内容社区

- 商铺查询与筛选
- 商铺类型管理
- 博客发布、浏览、点赞、评论
- 关注关系与信息流能力

### 3. 秒杀与优惠券

- 优惠券管理
- 秒杀库存扣减
- 异步下单与消息处理
- Redis 相关的并发控制与数据保护

### 4. AI 能力

- AI 聊天接口
- AI 工具调用与业务路由
- 可扩展的工具服务封装

## 本地运行

在启动前，建议先确认以下内容：

1. 已安装 Java 与 Maven
2. 已准备好 Redis、数据库、Kafka 等依赖服务
3. 已正确配置 `smartlife/src/main/resources/application.yml`
4. 已导入数据库初始化脚本 `smartlife/src/main/resources/smartlife-init.sql`

### 启动步骤

```bash
cd smartlife
mvn clean test
mvn spring-boot:run
```

如果项目使用了外部消息队列或 AI 服务，请先确保相关依赖服务已启动并完成连接参数配置。

## 配置文件

- `smartlife/src/main/resources/application.yml`：主配置文件
- `smartlife/src/main/resources/smartlife-init.sql`：数据库初始化脚本
- `smartlife/src/main/resources/seckill.lua`：秒杀相关 Lua 脚本
- `smartlife/src/main/resources/rate-limit.lua`：限流相关 Lua 脚本

## 开发建议

- 先确认数据库表结构与初始化数据是否完整
- 运行前检查 Redis、Kafka、AI 接口等外部依赖
- 修改缓存、限流、秒杀逻辑时，建议同步检查对应 Lua 脚本和消息消费代码
- 新增接口时，注意统一返回结构与异常处理

