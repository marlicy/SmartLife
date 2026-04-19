-- smartlife initialization script
-- Minimal schema needed by retained modules.

CREATE TABLE IF NOT EXISTS tb_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password VARCHAR(255) DEFAULT NULL,
  nick_name VARCHAR(255) NOT NULL,
  icon VARCHAR(255) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_user_info (
  user_id BIGINT PRIMARY KEY,
  city VARCHAR(64) DEFAULT NULL,
  introduce VARCHAR(128) DEFAULT NULL,
  fans INT DEFAULT 0,
  followee INT DEFAULT 0,
  gender TINYINT(1) DEFAULT NULL,
  birthday DATE DEFAULT NULL,
  credits INT DEFAULT 0,
  level TINYINT(1) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_shop (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  type_id BIGINT DEFAULT NULL,
  images VARCHAR(1024) DEFAULT NULL,
  area VARCHAR(255) DEFAULT NULL,
  address VARCHAR(255) DEFAULT NULL,
  x DOUBLE DEFAULT NULL,
  y DOUBLE DEFAULT NULL,
  avg_price BIGINT DEFAULT NULL,
  sold INT DEFAULT 0,
  comments INT DEFAULT 0,
  score INT DEFAULT 0,
  open_hours VARCHAR(64) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_voucher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  sub_title VARCHAR(255) DEFAULT NULL,
  rules VARCHAR(1024) DEFAULT NULL,
  pay_value BIGINT DEFAULT 0,
  actual_value BIGINT DEFAULT 0,
  type INT DEFAULT 0,
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_seckill_voucher (
  voucher_id BIGINT PRIMARY KEY,
  stock INT NOT NULL DEFAULT 0,
  begin_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_voucher_order (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  voucher_id BIGINT NOT NULL,
  pay_type INT DEFAULT NULL,
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  pay_time DATETIME DEFAULT NULL,
  use_time DATETIME DEFAULT NULL,
  refund_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_voucher (user_id, voucher_id),
  INDEX idx_status_create_time (status, create_time)
);

-- Seed sample records for smoke testing
INSERT INTO tb_shop (id, name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours)
VALUES (1, 'SmartLife 示例商铺', 1, '', '示例商圈', '示例地址', 116.397, 39.908, 100, 0, 0, 50, '09:00-21:00')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO tb_voucher (id, shop_id, title, sub_title, rules, pay_value, actual_value, type, status)
VALUES (1, 1, 'SmartLife 秒杀券', '示例优惠券', '仅限示例商铺使用', 1000, 500, 1, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO tb_seckill_voucher (voucher_id, stock, begin_time, end_time)
VALUES (1, 100, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 7 DAY)
ON DUPLICATE KEY UPDATE stock = VALUES(stock);
