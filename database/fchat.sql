/*
 Navicat Premium Data Transfer

 Source Server         : mysql4000
 Source Server Type    : MySQL
 Source Server Version : 80017
 Source Host           : localhost:4000
 Source Schema         : fchat

 Target Server Type    : MySQL
 Target Server Version : 80017
 File Encoding         : 65001

 Date: 29/06/2021 15:18:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for block_list
-- ----------------------------
DROP TABLE IF EXISTS `block_list`;
CREATE TABLE `block_list`  (
  `group_id` int(11) NOT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`group_id`, `user_id`) USING BTREE,
  INDEX `uids`(`user_id`) USING BTREE,
  CONSTRAINT `groupid` FOREIGN KEY (`group_id`) REFERENCES `group` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `uids` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for contact
-- ----------------------------
DROP TABLE IF EXISTS `contact`;
CREATE TABLE `contact`  (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`, `phone`) USING BTREE,
  CONSTRAINT `uId` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS `group`;
CREATE TABLE `group`  (
  `group_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT NULL,
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 84 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for group_member
-- ----------------------------
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member`  (
  `group_id` int(11) NOT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` enum('admin','default') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`group_id`, `user_id`) USING BTREE,
  INDEX `group_id`(`group_id`) USING BTREE,
  INDEX `user_id_gm`(`user_id`) USING BTREE,
  CONSTRAINT `group_id` FOREIGN KEY (`group_id`) REFERENCES `group` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user_id_gm` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for group_message
-- ----------------------------
DROP TABLE IF EXISTS `group_message`;
CREATE TABLE `group_message`  (
  `group_msg_id` int(11) NOT NULL AUTO_INCREMENT,
  `group_receiver_id` int(11) NOT NULL,
  `msg_id` int(11) NULL DEFAULT NULL,
  `sender_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`group_msg_id`) USING BTREE,
  INDEX `group_receiver`(`group_receiver_id`) USING BTREE,
  INDEX `msg_id_group`(`msg_id`) USING BTREE,
  INDEX `gr_sender_id`(`sender_id`) USING BTREE,
  CONSTRAINT `gr_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `group_receiver` FOREIGN KEY (`group_receiver_id`) REFERENCES `group` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `msg_id_group` FOREIGN KEY (`msg_id`) REFERENCES `message` (`msg_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 311 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for location
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location`  (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `latitude` double NULL DEFAULT NULL,
  `longitude` double NULL DEFAULT NULL,
  `date_change` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  CONSTRAINT `id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `msg_id` int(11) NOT NULL AUTO_INCREMENT,
  `create_date` timestamp(6) NULL DEFAULT NULL,
  `type` enum('text','img','video','audio','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'text',
  `status` enum('seen','default') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'default',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`msg_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1068 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `notification_id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sender_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_date` timestamp NOT NULL,
  `status` int(2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `userid`(`user_id`) USING BTREE,
  INDEX `senderid`(`sender_id`) USING BTREE,
  CONSTRAINT `senderid` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userid` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 117 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fullname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NoName',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `last_online` timestamp(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  `status` enum('on','off') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'off',
  `avartar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/image/2984c01d345c425f8898eb641168410e1618371995196.png',
  `public_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `token_client` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `phone`(`phone`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_follower
-- ----------------------------
DROP TABLE IF EXISTS `user_follower`;
CREATE TABLE `user_follower`  (
  `sender_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`sender_id`, `receiver_id`) USING BTREE,
  INDEX `sender`(`sender_id`) USING BTREE,
  INDEX `receiver`(`receiver_id`) USING BTREE,
  CONSTRAINT `receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_friend
-- ----------------------------
DROP TABLE IF EXISTS `user_friend`;
CREATE TABLE `user_friend`  (
  `user1_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user2_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`user1_id`, `user2_id`) USING BTREE,
  INDEX `user1_id`(`user1_id`) USING BTREE,
  INDEX `user2_id`(`user2_id`) USING BTREE,
  CONSTRAINT `user1_id` FOREIGN KEY (`user1_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user2_id` FOREIGN KEY (`user2_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_message
-- ----------------------------
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message`  (
  `user_msg_id` int(11) NOT NULL AUTO_INCREMENT,
  `receiver_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `msg_id` int(255) NOT NULL,
  `sender_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`user_msg_id`) USING BTREE,
  INDEX `receiver_id`(`receiver_id`) USING BTREE,
  INDEX `msg_id`(`msg_id`) USING BTREE,
  INDEX `sender_id`(`sender_id`) USING BTREE,
  CONSTRAINT `msg_id` FOREIGN KEY (`msg_id`) REFERENCES `message` (`msg_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 708 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
