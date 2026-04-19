# Docker 部署 Kafka

下面给出一个最常用、最简单的 Kafka 单机部署方式，适合本地开发和调试 `smartlife` 项目。

## 方案一：Kafka + ZooKeeper

### 1. 创建 `docker-compose.yml`
```yaml
version: '3.8'

services:
  zookeeper:
    image: bitnami/zookeeper:latest
    container_name: smartlife-zookeeper
    restart: unless-stopped
    ports:
      - '2181:2181'
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes

  kafka:
    image: bitnami/kafka:latest
    container_name: smartlife-kafka
    restart: unless-stopped
    ports:
      - '9092:9092'
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
      - ALLOW_PLAINTEXT_LISTENER=yes
    depends_on:
      - zookeeper
```

### 2. 启动
在 `docker-compose.yml` 所在目录执行：
```bash
docker compose up -d
```

### 3. 检查容器状态
```bash
docker ps
```

### 4. 创建 topic
进入 Kafka 容器后创建 topic：
```bash
docker exec -it smartlife-kafka bash
kafka-topics.sh --create --topic smartlife.seckill.order --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics.sh --create --topic smartlife.cache.rebuild --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### 5. 查看 topic
```bash
docker exec -it smartlife-kafka bash
kafka-topics.sh --list --bootstrap-server localhost:9092
```

---

## 方案二：Kafka KRaft 模式

如果你不想依赖 ZooKeeper，也可以用 KRaft 模式。这个方式更现代，但配置稍复杂。

如果你需要，我可以后面再给你补一份 KRaft 版的 `docker-compose.yml`。

---

## 6. `smartlife` 连接配置
你的 `application.yml` 中 Kafka 配置保持如下即可：

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

如果 `smartlife` 在 Docker 容器里运行，且 Kafka 也在 Docker 中，则 `bootstrap-servers` 需要改成容器网络地址，例如：

```yaml
spring:
  kafka:
    bootstrap-servers: smartlife-kafka:9092
```

---

## 7. 常见问题

### 端口占用
如果本机 `9092` 被占用，可以改映射端口，例如：
```yaml
ports:
  - '29092:9092'
```
然后把应用里的 Kafka 地址改成对应端口。

### 生产者连接失败
重点检查：
- `advertised.listeners`
- 防火墙
- `bootstrap-servers`

### 消费者收不到消息
重点检查：
- topic 是否创建成功
- 消费组是否正确
- 应用是否真的启动成功
