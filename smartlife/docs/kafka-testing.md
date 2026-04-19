# Kafka 在 smartlife 中怎么测试

## 1. 它为什么在接口测试里不明显
Kafka 在这个项目里主要做的是**异步处理**，所以你在前端或 Apifox 点接口时，通常只看到：

- 请求很快返回
- 但真正的数据写入是稍后由消费者完成

这就是 Kafka 的作用。

---

## 2. smartlife 里 Kafka 用在什么地方

### 订单异步落库
- 生产者：`SeckillOrderProducer`
- Topic：`smartlife.seckill.order`
- 消费者：`SeckillOrderConsumer`

用户秒杀成功后，接口先返回订单号，消息发到 Kafka，再由消费者写数据库。

### 缓存删除失败补偿
- 生产者：`CacheRebuildProducer`
- Topic：`smartlife.cache.rebuild`
- 消费者：`CacheRebuildConsumer`

当缓存删除失败时，通过 Kafka 发消息做重试。

---

## 3. 怎么观察 Kafka 是否真的工作了

### 方法 A 看后端日志
启动 `smartlife` 后，执行秒杀接口，然后看控制台日志：
- 能看到 `seckill order created asynchronously`，说明消费者接到了消息。

### 方法 B 看数据库
秒杀接口返回订单 ID 后，隔一会儿再看 `tb_voucher_order` 表：
- 如果刚返回时还没记录，过一会儿才出现，说明走了 Kafka 异步落库。

### 方法 C 看 Kafka topic
在 Kafka 所在虚拟机执行：

```bash
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

确认 topic 存在：
- `smartlife.seckill.order`
- `smartlife.cache.rebuild`

如果你想看消息积压，可以进一步用 consumer 工具或者看应用日志。

---

## 4. 推荐测试步骤

1. 启动 Kafka
2. 启动 smartlife
3. 登录拿 token
4. 打开前端页面，点“秒杀”
5. 立即查看接口返回订单号
6. 再看数据库和控制台日志

---

## 5. 如果你想更明显感受到 Kafka 的作用
你可以：
- 临时把消费者停掉
- 再调用秒杀接口
- 你会看到接口还是返回了订单号，但数据库不立即落库
- 这就是 Kafka 缓冲和解耦的效果
