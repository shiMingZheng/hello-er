-- ========================================
-- ERP 系统数据库初始化脚本（PostgreSQL）
-- ========================================

-- 创建数据库
CREATE DATABASE erp_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- 连接到数据库
\c erp_db;

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    customer_id BIGINT,
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_customer_id ON "user"(customer_id);
COMMENT ON TABLE "user" IS '用户表';

-- ========================================
-- 2. 客户表
-- ========================================
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    phone VARCHAR(20),
    address VARCHAR(255),
    level VARCHAR(20) DEFAULT 'NORMAL',
    credit_limit NUMERIC(10, 2) DEFAULT 0.00,
    balance NUMERIC(10, 2) DEFAULT 0.00,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE customer IS '客户表';

-- ========================================
-- 3. 商品表
-- ========================================
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    image VARCHAR(255),
    normal_price NUMERIC(10, 2) NOT NULL,
    vip_price NUMERIC(10, 2) NOT NULL,
    stock INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_status ON product(status);
COMMENT ON TABLE product IS '商品表';

-- ========================================
-- 4. 订单主表
-- ========================================
CREATE TABLE "order" (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    address VARCHAR(255),
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_customer_id ON "order"(customer_id);
CREATE INDEX idx_order_no ON "order"(order_no);
CREATE INDEX idx_order_status ON "order"(status);
CREATE INDEX idx_order_create_time ON "order"(create_time);
COMMENT ON TABLE "order" IS '订单主表';

-- ========================================
-- 5. 订单明细表
-- ========================================
CREATE TABLE order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_order_item_order_id ON order_item(order_id);
COMMENT ON TABLE order_item IS '订单明细表';

-- ========================================
-- 6. 应收账款表
-- ========================================
CREATE TABLE receivable (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    paid_amount NUMERIC(10, 2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'UNPAID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_receivable_order_id ON receivable(order_id);
CREATE INDEX idx_receivable_customer_id ON receivable(customer_id);
COMMENT ON TABLE receivable IS '应收账款表';

-- ========================================
-- 7. 收款记录表
-- ========================================
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    remark TEXT,
    operator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_customer_id ON payment(customer_id);
COMMENT ON TABLE payment IS '收款记录表';

-- ========================================
-- 8. 收款核销关联表
-- ========================================
CREATE TABLE payment_receivable (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    receivable_id BIGINT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_receivable_payment_id ON payment_receivable(payment_id);
CREATE INDEX idx_payment_receivable_receivable_id ON payment_receivable(receivable_id);
COMMENT ON TABLE payment_receivable IS '收款核销关联表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入管理员账号（密码：admin123，已加密）
-- 注意：这里的密码是示例，实际使用时需要通过后端 PasswordUtils.encode() 生成
INSERT INTO "user" (username, password, role, status) 
VALUES ('admin', 'salt$hash', 'ADMIN', 1);

-- 插入测试商品
INSERT INTO product (name, image, normal_price, vip_price, stock, status)
VALUES 
    ('高档A4打印纸', '/static/logo.png', 29.90, 25.00, 100, 1),
    ('得力订书机', '/static/logo.png', 15.50, 13.00, 50, 1),
    ('晨光签字笔 (10支装)', '/static/logo.png', 9.90, 8.50, 200, 1);

-- ========================================
-- 完成
-- ========================================
SELECT '✅ 数据库初始化完成！' AS message;
