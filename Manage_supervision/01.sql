/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : 509

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 20/05/2025 10:58:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for after_sale
-- ----------------------------
DROP TABLE IF EXISTS `after_sale`;
CREATE TABLE `after_sale`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'refund' COMMENT '售后类型: refund-仅退款, return-退货退款',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请原因',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '凭证图片，多张以逗号分隔',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态: pending-处理中, approved-已同意, rejected-已拒绝, completed-已完成',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商家处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '售后申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of after_sale
-- ----------------------------
INSERT INTO `after_sale` VALUES (2, 5, 4, 'refund', '111111111111', NULL, 'rejected', '滚！！！！！！！！', NULL, '2025-05-19 17:26:32', '2025-05-19 17:26:42', 0);
INSERT INTO `after_sale` VALUES (3, 6, 4, 'refund', '11111111', NULL, 'rejected', '111111111111', NULL, '2025-05-19 17:30:39', '2025-05-19 17:30:52', 0);

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `FK_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (20, '数码', NULL, '2025-05-19 13:36:43');
INSERT INTO `categories` VALUES (21, '手机', 20, '2025-05-19 13:36:49');
INSERT INTO `categories` VALUES (22, '智能手机', 21, '2025-05-19 13:36:55');
INSERT INTO `categories` VALUES (23, '翻盖手机', 21, '2025-05-19 13:37:03');
INSERT INTO `categories` VALUES (24, '华为', 22, '2025-05-19 13:37:13');
INSERT INTO `categories` VALUES (25, 'oppo', 22, '2025-05-19 13:37:23');
INSERT INTO `categories` VALUES (26, '三星', 23, '2025-05-19 13:37:28');
INSERT INTO `categories` VALUES (27, 'huawei nova系列', 24, '2025-05-19 13:37:36');
INSERT INTO `categories` VALUES (28, '三星w2025', 26, '2025-05-19 13:37:47');
INSERT INTO `categories` VALUES (29, '三星w2019', 26, '2025-05-19 23:29:42');
INSERT INTO `categories` VALUES (30, 'mate系列', 24, '2025-05-19 23:41:46');

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NULL DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  `recipient_id` bigint NOT NULL,
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sent_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_messages_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_chat_messages_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_chat_messages_recipient_id`(`recipient_id` ASC) USING BTREE,
  CONSTRAINT `FK_chat_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_chat_messages_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_chat_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 1, 4, 3, '您好，我想咨询订单 2025051917302082f26a 的相关问题', '2025-05-19 17:41:00', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (2, 1, 3, 4, '你谁？', '2025-05-19 17:41:08', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (3, 1, 4, 3, '你妈了逼', '2025-05-19 17:41:13', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (4, 1, 4, 3, '您好，我想咨询订单 2025051917240916ea3b 的相关问题', '2025-05-19 17:41:20', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (5, 1, 3, 4, '怎么骂人呢', '2025-05-19 17:44:03', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (6, 1, 4, 3, '呵呵', '2025-05-19 17:44:09', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (7, 1, 4, 3, '？', '2025-05-19 17:44:36', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (8, 1, 4, 3, '1', '2025-05-19 17:44:49', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (9, 1, 4, 3, '1、', '2025-05-19 17:44:57', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (10, 1, 4, 3, '1', '2025-05-19 17:45:07', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (11, 1, 4, 3, '1', '2025-05-19 17:45:14', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (12, 1, 4, 3, '1', '2025-05-19 17:46:20', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (13, 1, 4, 3, '1', '2025-05-19 17:46:43', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (14, 1, 4, 3, '1', '2025-05-19 17:46:48', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (15, 1, 4, 3, '1', '2025-05-19 17:46:49', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (16, 1, 4, 3, '1', '2025-05-19 17:46:49', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (17, 1, 4, 3, '1', '2025-05-19 17:46:49', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (18, 1, 4, 3, '1', '2025-05-19 17:46:50', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (19, 1, 4, 3, '1', '2025-05-19 17:46:57', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (20, 1, 4, 3, '1', '2025-05-19 17:47:50', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (21, 1, 4, 3, '2', '2025-05-19 17:47:56', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (22, 1, 4, 3, '？？？？？？', '2025-05-19 17:48:04', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (23, 1, 4, 3, '1', '2025-05-19 17:48:16', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (24, 1, 4, 3, '1', '2025-05-19 17:48:16', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (25, 1, 4, 3, '1', '2025-05-19 17:48:20', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (26, 1, 3, 4, '1', '2025-05-19 17:48:31', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (27, 1, 3, 4, '1', '2025-05-19 17:48:32', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (28, 1, 3, 4, '1', '2025-05-19 17:48:42', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (29, 1, 3, 4, '1', '2025-05-19 17:48:44', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (30, 1, 4, 3, '1', '2025-05-19 17:49:48', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (31, 1, 4, 3, '1', '2025-05-19 17:49:49', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (32, 1, 4, 3, '3', '2025-05-19 17:49:50', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (33, 1, 3, 4, '1', '2025-05-19 17:49:56', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (34, 1, 3, 4, '1', '2025-05-19 17:49:57', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (35, 1, 3, 4, '1', '2025-05-19 17:50:01', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (36, 1, 3, 4, '1', '2025-05-19 17:50:01', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (37, 1, 4, 3, '您好，我想咨询订单 2025051917302082f26a 的相关问题', '2025-05-19 17:50:17', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (38, 1, 4, 3, '您好，我想咨询订单 2025051923484377923e 的相关问题', '2025-05-19 23:49:06', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (39, 1, 3, 4, '？', '2025-05-19 23:49:11', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (40, 1, 4, 3, '你好', '2025-05-20 01:23:10', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (41, 1, 3, 4, '你也好', '2025-05-20 01:23:16', 1, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for conversations
-- ----------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user1_id` bigint NOT NULL,
  `user2_id` bigint NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_message_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `unread_count_user1` int NOT NULL DEFAULT 0,
  `unread_count_user2` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversations_user1_id`(`user1_id` ASC) USING BTREE,
  INDEX `idx_conversations_user2_id`(`user2_id` ASC) USING BTREE,
  CONSTRAINT `FK_conversations_user1` FOREIGN KEY (`user1_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_conversations_user2` FOREIGN KEY (`user2_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversations
-- ----------------------------
INSERT INTO `conversations` VALUES (1, 4, 3, '2025-05-19 17:40:56', '2025-05-20 01:23:16', 0, 0);

-- ----------------------------
-- Table structure for merchant_income
-- ----------------------------
DROP TABLE IF EXISTS `merchant_income`;
CREATE TABLE `merchant_income`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收入记录ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_item_id` bigint NOT NULL COMMENT '订单项ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：income-收入, refund-退款',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商家收入记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchant_income
-- ----------------------------

-- ----------------------------
-- Table structure for merchant_user_relations
-- ----------------------------
DROP TABLE IF EXISTS `merchant_user_relations`;
CREATE TABLE `merchant_user_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `assign_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_merchant_user`(`merchant_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_merchant_user_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_merchant_user_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK_merchant_user_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_merchant_user_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchant_user_relations
-- ----------------------------

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_price` decimal(10, 2) NOT NULL,
  `quantity` int NOT NULL,
  `subtotal` decimal(10, 2) NOT NULL,
  `merchant_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_item_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_item_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_order_item_merchant_id`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `FK_order_item_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (4, 4, 2, '华为nova100', 2799.00, 1, 2799.00, 3);
INSERT INTO `order_items` VALUES (5, 5, 2, '华为nova100', 2799.00, 1, 2799.00, 3);
INSERT INTO `order_items` VALUES (6, 6, 2, '华为nova100', 2799.00, 1, 2799.00, 3);
INSERT INTO `order_items` VALUES (7, 7, 4, '华为mate800', 9000.00, 2, 18000.00, 3);
INSERT INTO `order_items` VALUES (8, 1, 4, '华为mate800', 9000.00, 1, 9000.00, 3);
INSERT INTO `order_items` VALUES (9, 2, 5, '华为noca11', 1633.00, 2, 3266.00, 3);
INSERT INTO `order_items` VALUES (10, 3, 5, '华为noca11', 1633.00, 1, 1633.00, 3);
INSERT INTO `order_items` VALUES (11, 4, 4, '华为mate800', 9000.00, 1, 9000.00, 3);
INSERT INTO `order_items` VALUES (12, 5, 5, '华为noca11', 1633.00, 1, 1633.00, 3);
INSERT INTO `order_items` VALUES (13, 6, 4, '华为mate800', 9000.00, 1, 9000.00, 3);
INSERT INTO `order_items` VALUES (14, 7, 5, '华为noca11', 1633.00, 3, 4899.00, 3);
INSERT INTO `order_items` VALUES (15, 8, 5, '华为noca11', 1633.00, 1, 1633.00, 3);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单状态：pending-待支付, paid-已支付, shipped-已发货, completed-已完成, cancelled-已取消, refunded-已退款, after_sale-售后中, after_sale_rejected-售后拒绝',
  `after_sale_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '售后状态：pending-处理中, approved-已同意, rejected-已拒绝',
  `after_sale_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '售后原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `recipient_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人姓名',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_number`(`order_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, '20250520004852663988', 4, 9000.00, 'after_sale_rejected', 'rejected', '1', '2025-05-20 00:48:52', '2025-05-20 00:49:20', '1', '13384403671', '1', '1\n售后处理：拒绝退款，原因：？');
INSERT INTO `orders` VALUES (2, '2025052001233591371d', 4, 3266.00, 'cancelled', NULL, NULL, '2025-05-20 01:23:35', '2025-05-20 01:23:42', '1', '13121122122', '131', '1');
INSERT INTO `orders` VALUES (3, '202505200124050f9950', 4, 1633.00, 'after_sale_rejected', 'rejected', '1111111111', '2025-05-20 01:24:05', '2025-05-20 01:24:41', '1', '13112112341', '11', '1\n售后处理：拒绝退款，原因：？？？？？？？？');
INSERT INTO `orders` VALUES (4, '202505200125015304b8', 4, 9000.00, 'completed', NULL, NULL, '2025-05-20 01:25:01', '2025-05-20 01:25:21', '1', '13211311311', '112', '1');
INSERT INTO `orders` VALUES (5, '2025052001373635ea17', 4, 1633.00, 'after_sale', 'pending', '？？？？？？？', '2025-05-20 01:37:37', '2025-05-20 01:37:52', '1', '13384403671', '1', '1');
INSERT INTO `orders` VALUES (6, '202505200138121d2e80', 4, 9000.00, 'paid', NULL, NULL, '2025-05-20 01:38:12', '2025-05-20 01:38:18', '1', '13384403671', '4', '1');
INSERT INTO `orders` VALUES (7, '20250520021258bcde75', 4, 4899.00, 'paid', NULL, NULL, '2025-05-20 02:12:59', '2025-05-20 02:13:06', '1', '13384403671', '1', '1');
INSERT INTO `orders` VALUES (8, '202505200217233decf6', 4, 1633.00, 'paid', NULL, NULL, '2025-05-20 02:17:24', '2025-05-20 02:17:29', '1', '13384403671', '1', '1');

-- ----------------------------
-- Table structure for payments
-- ----------------------------
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `payment_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `refund_time` datetime NULL DEFAULT NULL,
  `refund_amount` decimal(10, 2) NULL DEFAULT NULL,
  `refund_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `payment_number`(`payment_number` ASC) USING BTREE,
  INDEX `idx_payment_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_payment_number`(`payment_number` ASC) USING BTREE,
  INDEX `idx_payment_status`(`status` ASC) USING BTREE,
  CONSTRAINT `FK_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payments
-- ----------------------------
INSERT INTO `payments` VALUES (7, 5, 'PAY20250519172605d3e1ac', 2799.00, 'alipay', 'cancelled', '2025-05-19 17:26:06', '2025-05-19 17:26:07', NULL, NULL, NULL, NULL);
INSERT INTO `payments` VALUES (8, 5, 'PAY20250519172608732c2f', 2799.00, 'alipay', 'completed', '2025-05-19 17:26:09', '2025-05-19 17:26:11', 'TXN17476467711416ee900a1fa', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (9, 6, 'PAY202505191730235a6559', 2799.00, 'alipay', 'completed', '2025-05-19 17:30:23', '2025-05-19 17:30:26', 'TXN1747647025512a312934c26', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (10, 7, 'PAY20250519234846dd1183', 18000.00, 'alipay', 'completed', '2025-05-19 23:48:47', '2025-05-19 23:48:50', 'TXN1747669730077624b043fac', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (11, 1, 'PAY20250520004854fa8762', 9000.00, 'credit_card', 'completed', '2025-05-20 00:48:55', '2025-05-20 00:48:58', 'TXN17476733375490766a3131a', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (12, 2, 'PAY2025052001233821d0a7', 3266.00, 'alipay', 'cancelled', '2025-05-20 01:23:38', '2025-05-20 01:23:39', NULL, NULL, NULL, NULL);
INSERT INTO `payments` VALUES (13, 3, 'PAY20250520012407ff8b26', 1633.00, 'wechat', 'completed', '2025-05-20 01:24:08', '2025-05-20 01:24:11', 'TXN1747675450599ff6aee2fb0', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (14, 4, 'PAY20250520012503027bcf', 9000.00, 'wechat', 'completed', '2025-05-20 01:25:04', '2025-05-20 01:25:06', 'TXN174767550593180a413afbf', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (15, 5, 'PAY20250520013739328d21', 1633.00, 'alipay', 'completed', '2025-05-20 01:37:40', '2025-05-20 01:37:42', 'TXN1747676262293a12f326cc4', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (16, 6, 'PAY20250520013814e92bf5', 9000.00, 'credit_card', 'completed', '2025-05-20 01:38:15', '2025-05-20 01:38:18', 'TXN1747676298194a748038371', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (17, 7, 'PAY20250520021303210bda', 4899.00, 'wechat', 'completed', '2025-05-20 02:13:04', '2025-05-20 02:13:06', 'TXN1747678386421049e238023', NULL, NULL, NULL);
INSERT INTO `payments` VALUES (18, 8, 'PAY202505200217267d1ad2', 1633.00, 'credit_card', 'completed', '2025-05-20 02:17:26', '2025-05-20 02:17:29', 'TXN174767864893334c748b92d', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock` int NOT NULL DEFAULT 0,
  `category_id` bigint NULL DEFAULT NULL,
  `merchant_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending',
  `audit_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `audit_time` datetime NULL DEFAULT NULL,
  `audit_user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK_product_audit_user`(`audit_user_id` ASC) USING BTREE,
  INDEX `idx_product_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_product_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_product_status`(`status` ASC) USING BTREE,
  INDEX `idx_product_name`(`name` ASC) USING BTREE,
  CONSTRAINT `FK_product_audit_user` FOREIGN KEY (`audit_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `FK_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `FK_product_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, '华为nova11', '华为手机', '/uploads/3b329f5a-9f68-4b5e-807c-042ca781615a.jpg,/uploads/c97b5392-f565-4fd5-81bd-20f5ad786e28.jpg,/uploads/c2a8f7da-57a9-4d48-b900-e7b33115fcc3.jpg', 0.00, 0, 27, 3, '2025-05-19 14:07:10', '2025-05-19 14:07:10', 'pending', NULL, NULL, NULL);
INSERT INTO `products` VALUES (2, '华为nova100', '超级厉害的华为', '/uploads/f366dd6f-0a9a-4dd7-a370-c1b36affad6d.jpg,/uploads/da7666af-0c3b-45aa-b5ea-1a312343b752.jpg,/uploads/9374e571-7f52-4b9f-acbb-4137b67db50a.jpg', 2799.00, 153, 27, 3, '2025-05-19 14:13:19', '2025-05-19 14:18:59', 'approved', '', '2025-05-19 14:19:10', NULL);
INSERT INTO `products` VALUES (3, '华为mate60', '这是一个手机', '/uploads/7eb3bac1-a767-4dea-b451-da101a202260.jpg,/uploads/a12a4cd6-19ee-48bf-8774-3e66320b9f38.jpg', 6000.00, 120, 30, 3, '2025-05-19 23:43:46', '2025-05-19 23:43:46', 'rejected', '不准通过', '2025-05-19 23:44:03', NULL);
INSERT INTO `products` VALUES (4, '华为mate800', '这是一个手机11', '/uploads/a8b39dba-9751-499b-9428-746cd1bd4742.jpg,/uploads/8a5e0225-3b4d-4450-88e8-1a1196ad35ed.jpg', 9000.00, 155, 30, 3, '2025-05-19 23:47:21', '2025-05-19 23:47:46', 'approved', '\n', '2025-05-19 23:47:57', NULL);
INSERT INTO `products` VALUES (5, '华为noca11', '11111111', '/uploads/eec44948-7f52-4af1-b063-f6b361a322ba.jpg,/uploads/f0ea101b-967c-44ca-b36b-b674c8f69266.jpg,/uploads/e046246a-1ffb-49b2-a7e1-738a99597456.jpg', 1633.00, 20, 27, 3, '2025-05-20 01:22:17', '2025-05-20 01:22:35', 'approved', '11111', '2025-05-20 01:22:44', NULL);

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `permissions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'ADMIN', '管理员角色，拥有最高权限', 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS,CATEGORY_VIEW,CATEGORY_EDIT,CATEGORY_DELETE,PRODUCT_AUDIT,PRODUCT_VIEW', '2025-05-19 03:36:47');
INSERT INTO `roles` VALUES (2, 'USER', '用户角色，基本用户权限', 'USER_VIEW,CATEGORY_VIEW,PRODUCT_VIEW', '2025-05-19 03:36:47');
INSERT INTO `roles` VALUES (3, 'MERCHANT', '商家角色，可以管理用户', 'USER_VIEW,USER_EDIT,USER_MANAGEMENT,USER_PROGRESS_VIEW,CATEGORY_VIEW,PRODUCT_MANAGEMENT,PRODUCT_VIEW', '2025-05-19 03:36:47');

-- ----------------------------
-- Table structure for user_addresses
-- ----------------------------
DROP TABLE IF EXISTS `user_addresses`;
CREATE TABLE `user_addresses`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `recipient_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货地址',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认地址，1-是，0-否',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户收货地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_addresses
-- ----------------------------

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `FK_user_roles_role`(`role_id` ASC) USING BTREE,
  CONSTRAINT `FK_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (5, 1);
INSERT INTO `user_roles` VALUES (4, 2);
INSERT INTO `user_roles` VALUES (3, 3);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active',
  `user_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `user_number`(`user_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (3, '2', '$2a$12$cEYLpBhMbzWYv4fH6Buc.OA2FC4emfGwzY.OsXR9vfC8V8RXn/ZE6', '2025-05-19 12:32:45', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25532795');
INSERT INTO `users` VALUES (4, '3', '$2a$12$ki5ZDgW4qvCx4hW4yVaz9uyfczDaMzd.2QVn4GZMUNlVnx4Yb4NK6', '2025-05-19 12:32:49', '/uploads/d80d2557-82e7-4546-8226-2b540a71517a.jpg', NULL, NULL, NULL, NULL, NULL, 'active', 'S25697894');
INSERT INTO `users` VALUES (5, 'tsuhata', '$2a$12$TzbSVVjYRjavUngsw97YLOuC6IDHYkvtRMxzPsB/Ej9OHj9nxhH2O', '2025-05-20 10:54:29', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25727344');

SET FOREIGN_KEY_CHECKS = 1;
