# smartlife 接口测试清单与 Redis Key 说明

## 一、接口测试清单

### 1. 用户登录
- `POST /user/code?phone=13800000000`
- `POST /user/login`

请求体示例：
```json
{
  "phone": "13800000000",
  "code": "123456"
}
```

### 2. 商铺查询
- `GET /shop/1`
- `PUT /shop`

### 3. 优惠券相关
- `POST /voucher/seckill`
- `POST /voucher-order/seckill/1`

### 4. 智能客服
- `POST /chat`

请求体示例：
```json
{
  "sessionId": "u1001",
  "message": "帮我介绍一下平台"
}
```

### 5. 限流验证
连续快速请求以下接口即可观察限流效果：
- `/user/code`
- `/user/login`
- `/chat`

---

## 二、Redis Key 说明

### 登录相关
- `login:code:{phone}`：短信验证码
- `login:token:{token}`：登录用户信息 Hash

### 商铺缓存
- `cache:shop:{id}`：商铺缓存
- `local:shop:{id}`：本地缓存（Caffeine 逻辑上使用）
- `lock:shop:{id}`：商铺缓存重建互斥锁

### 秒杀相关
- `seckill:stock:{voucherId}`：秒杀库存
- `seckill:order:{voucherId}`：已下单用户 Set
- `lock:seckill:stock:{voucherId}`：秒杀相关锁

### 限流相关
- `rate:limit:{key}`：滑动窗口限流 ZSet

### AI 会话
- `chat:memory:{sessionId}`：智能客服历史消息

### 预约相关
- `ai:reservation:{sessionId}:{reservationId}`：AI 预约记录

---

## 三、Kafka Topic 说明

### `smartlife.seckill.order`
用于秒杀订单异步落库。

### `smartlife.cache.rebuild`
用于缓存删除失败后的重试补偿。

---

## 四、启动顺序建议

1. MySQL
2. Redis
3. Kafka
4. smartlife 应用

---

## 五、测试建议

- 先跑 `smartlife-init.sql`
- 再用示例优惠券 `id=1` 测试秒杀
- AI 功能默认是降级模式，需配置 `smartlife.ai.enabled=true` 和真实 API Key 才会调用模型
