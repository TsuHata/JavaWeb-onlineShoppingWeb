-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除已有表（如果存在）
DROP TABLE IF EXISTS merchant_user_relations;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS conversations;
DROP TABLE IF EXISTS task_evaluations;
DROP TABLE IF EXISTS task_submissions;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS payments;

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建角色表
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    permissions VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    avatar VARCHAR(255),
    real_name VARCHAR(50),
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    bio TEXT,
    status VARCHAR(20) DEFAULT 'active',
    user_number VARCHAR(20) UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建用户-角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建会话表
CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    unread_count_user1 INT NOT NULL DEFAULT 0,
    unread_count_user2 INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_conversations_user1 FOREIGN KEY (user1_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_conversations_user2 FOREIGN KEY (user2_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建聊天消息表
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    sent_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    file_url VARCHAR(255),
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    file_size BIGINT,
    CONSTRAINT FK_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT FK_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_chat_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建商家-用户关系表
CREATE TABLE merchant_user_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    CONSTRAINT FK_merchant_user_merchant FOREIGN KEY (merchant_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_merchant_user_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT UK_merchant_user UNIQUE (merchant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建分类表
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_category_parent FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建商品表
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    merchant_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'pending', -- pending, approved, rejected
    audit_comment TEXT,
    audit_time DATETIME,
    audit_user_id BIGINT,
    CONSTRAINT FK_product_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT FK_product_merchant FOREIGN KEY (merchant_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_product_audit_user FOREIGN KEY (audit_user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建订单表
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) NOT NULL COMMENT '订单状态：pending-待支付, paid-已支付, shipped-已发货, completed-已完成, cancelled-已取消, refunded-已退款, after_sale-售后中, after_sale_rejected-售后拒绝',
    after_sale_status VARCHAR(20) COMMENT '售后状态：pending-处理中, approved-已同意, rejected-已拒绝',
    after_sale_reason TEXT COMMENT '售后原因',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    address VARCHAR(255) NOT NULL COMMENT '收货地址',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    recipient_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    remark TEXT COMMENT '备注',
    UNIQUE KEY uk_order_number (order_number)
) COMMENT='订单表';

-- 创建订单商品表
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    merchant_id BIGINT NOT NULL,
    CONSTRAINT FK_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT FK_order_item_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT FK_order_item_merchant FOREIGN KEY (merchant_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建支付表
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- alipay, wechat, credit_card
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, completed, failed, refunded
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(100),
    refund_time DATETIME,
    refund_amount DECIMAL(10, 2),
    refund_reason TEXT,
    CONSTRAINT FK_payment_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引以提高查询性能
CREATE INDEX idx_conversations_user1_id ON conversations(user1_id);
CREATE INDEX idx_conversations_user2_id ON conversations(user2_id);
CREATE INDEX idx_chat_messages_conversation_id ON chat_messages(conversation_id);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_recipient_id ON chat_messages(recipient_id);
CREATE INDEX idx_merchant_user_merchant_id ON merchant_user_relations(merchant_id);
CREATE INDEX idx_merchant_user_user_id ON merchant_user_relations(user_id);
CREATE INDEX idx_category_parent_id ON categories(parent_id);
CREATE INDEX idx_product_category_id ON products(category_id);
CREATE INDEX idx_product_merchant_id ON products(merchant_id);
CREATE INDEX idx_product_status ON products(status);
CREATE INDEX idx_product_name ON products(name);

-- 为订单表创建索引
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_status ON orders(status);

-- 为订单商品表创建索引
CREATE INDEX idx_order_item_order_id ON order_items(order_id);
CREATE INDEX idx_order_item_product_id ON order_items(product_id);
CREATE INDEX idx_order_item_merchant_id ON order_items(merchant_id);

-- 为支付表创建索引
CREATE INDEX idx_payment_order_id ON payments(order_id);
CREATE INDEX idx_payment_number ON payments(payment_number);
CREATE INDEX idx_payment_status ON payments(status);

-- 插入基本角色数据
INSERT INTO roles (name, description, permissions, create_time) VALUES 
('ADMIN', '管理员角色，拥有最高权限', ',USER_VIEW,ROLE_VIEW,LOG_VIEW,USER_EDIT,ROLE_EDIT,SYSTEM_SETTINGS,USER_DELETE,ROLE_DELETE,CATEGORY_VIEW,CATEGORY_EDIT,CATEGORY_DELETE,PRODUCT_AUDIT', NOW()),
('USER', '用户角色，基本用户权限', ',CATEGORY_VIEW,PRODUCT_VIEW', NOW()),
('MERCHANT', '商家角色，可以管理用户', ',USER_VIEW,USER_EDIT,USER_MANAGEMENT,USER_PROGRESS_VIEW,CATEGORY_VIEW,PRODUCT_MANAGEMENT', NOW());

-- 插入管理员用户 (username: admin, password: 123456)
-- 密码使用BCrypt加密，这里是"123456"的BCrypt哈希值
INSERT INTO users (username, password, real_name, nickname, email, status, create_time) VALUES 
('admin', '$2a$10$X7aPRYS9WF0cGHV9lOJhQO3YfpJiA4SZ5uE5MUmjHjjQEa5LsPzWe', '系统管理员', 'Admin', 'admin@example.com', 'active', NOW());

-- 为管理员用户分配ADMIN角色
INSERT INTO user_roles (user_id, role_id) SELECT 
(SELECT id FROM users WHERE username = 'admin'), 
(SELECT id FROM roles WHERE name = 'ADMIN');

-- 确保roles表中的权限字段包含正确的权限设置
UPDATE roles SET permissions = 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS,CATEGORY_VIEW,CATEGORY_EDIT,CATEGORY_DELETE,PRODUCT_AUDIT,PRODUCT_VIEW' WHERE name = 'ADMIN';
UPDATE roles SET permissions = 'USER_VIEW,USER_EDIT,USER_MANAGEMENT,USER_PROGRESS_VIEW,CATEGORY_VIEW,PRODUCT_MANAGEMENT,PRODUCT_VIEW' WHERE name = 'MERCHANT';
UPDATE roles SET permissions = 'USER_VIEW,CATEGORY_VIEW,PRODUCT_VIEW' WHERE name = 'USER';

-- 插入初始分类数据
INSERT INTO categories (name, parent_id, create_time) VALUES 
-- 根分类
('系统管理', NULL, NOW()),
('用户管理', NULL, NOW()),
('内容管理', NULL, NOW()),
('数据分析', NULL, NOW()),

-- 系统管理的子分类
('权限设置', 1, NOW()),
('系统配置', 1, NOW()),
('日志管理', 1, NOW()),

-- 用户管理的子分类
('用户信息', 2, NOW()),
('角色管理', 2, NOW()),
('用户审核', 2, NOW()),

-- 内容管理的子分类
('文档管理', 3, NOW()),
('消息管理', 3, NOW()),
('附件管理', 3, NOW()),

-- 数据分析的子分类
('统计报表', 4, NOW()),
('数据导出', 4, NOW()),
('数据可视化', 4, NOW()),

-- 三级分类示例
('操作日志', 7, NOW()),
('登录日志', 7, NOW()),
('异常日志', 7, NOW()); 