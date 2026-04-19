# smartlife

SmartLife is an enhanced local-life service demo project evolved from the original hmdp baseline.

## Included capabilities

- SMS code login with Redis session storage
- Dual interceptor auth flow
- Shop cache with pass-through, mutex, logical-expire support
- Seckill vouchers with Redis Lua + Kafka async order flow + Redisson lock
- Scheduled timeout order closing
- Sliding-window rate limiting with Redis + Lua
- AI customer service with Redis memory and tool-router style function calling

## Skipped by design

The following modules from the original document are intentionally not implemented in this version:

- Blog / likes / follow / feed stream
- GEO nearby shop search
- Sign-in and continuous sign-in
- UV counting with HyperLogLog

## Runtime requirements

- Java 17
- MySQL 8+
- Redis 6+
- Kafka 3+

## Database initialization

Run `src/main/resources/smartlife-init.sql` against the `hmdp` database.

## Configuration

Update `src/main/resources/application.yml` with your local values:

- MySQL credentials
- Redis credentials
- Kafka bootstrap servers
- `smartlife.ai.enabled`
- `smartlife.ai.api-key`

## Main endpoints

- `POST /user/code` - send SMS code
- `POST /user/login` - login and receive token
- `GET /shop/{id}` - query shop by id
- `PUT /shop` - update shop
- `POST /voucher/seckill` - create seckill voucher
- `POST /voucher-order/seckill/{id}` - seckill order
- `POST /chat` - AI customer service

## Build

```bash
mvn clean compile
```

## Docs

- `docs/smartlife-keys-and-tests.md` - 接口测试清单、Redis Key、Kafka topic 说明
- `docs/docker-kafka.md` - Docker 部署 Kafka 指南
