/*
 Navicat MySQL Data Transfer

 Source Server         : 本地测试环境
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 07/08/2023 00:03:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for db_account
-- ----------------------------
DROP TABLE IF EXISTS `db_account`;
CREATE TABLE `db_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `register_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_email` (`email`),
  UNIQUE KEY `unique_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of db_account
-- ----------------------------
BEGIN;
COMMIT;

ALTER TABLE `db_account`
  MODIFY `username` varchar(64) NOT NULL,
  MODIFY `email` varchar(128) NOT NULL,
  MODIFY `password` varchar(255) NOT NULL,
  MODIFY `role` varchar(32) NOT NULL DEFAULT 'user',
  ADD COLUMN `avatar` varchar(255) DEFAULT NULL AFTER `role`,
  ADD COLUMN `mute` tinyint(1) NOT NULL DEFAULT 0 AFTER `register_time`,
  ADD COLUMN `banned` tinyint(1) NOT NULL DEFAULT 0 AFTER `mute`;

DROP TABLE IF EXISTS `db_account_details`;
CREATE TABLE `db_account_details` (
  `id` int NOT NULL,
  `gender` int DEFAULT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `qq` varchar(32) DEFAULT NULL,
  `wx` varchar(64) DEFAULT NULL,
  `desc` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_account_privacy`;
CREATE TABLE `db_account_privacy` (
  `id` int NOT NULL,
  `phone` tinyint(1) NOT NULL DEFAULT 1,
  `email` tinyint(1) NOT NULL DEFAULT 1,
  `wx` tinyint(1) NOT NULL DEFAULT 1,
  `qq` tinyint(1) NOT NULL DEFAULT 1,
  `gender` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_email_record`;
CREATE TABLE `db_email_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(128) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0 sending, 1 success, 2 failed',
  PRIMARY KEY (`id`),
  KEY `idx_email_record_status` (`status`),
  KEY `idx_email_record_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_image_store`;
CREATE TABLE `db_image_store` (
  `uid` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`, `name`),
  KEY `idx_image_store_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_notification`;
CREATE TABLE `db_notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` int NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` varchar(1024) NOT NULL,
  `type` varchar(32) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `time` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_topic_type`;
CREATE TABLE `db_topic_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `desc` varchar(255) DEFAULT NULL,
  `color` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_topic`;
CREATE TABLE `db_topic` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `intro` varchar(512) DEFAULT NULL,
  `content` longtext NOT NULL,
  `type` int NOT NULL,
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `uid` int NOT NULL,
  `top` tinyint(1) NOT NULL DEFAULT 0,
  `locked` tinyint(1) NOT NULL DEFAULT 0,
  `invisible` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_topic_uid` (`uid`),
  KEY `idx_topic_type_time` (`type`, `time`),
  KEY `idx_topic_visible_time` (`invisible`, `time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_topic_comment`;
CREATE TABLE `db_topic_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` int NOT NULL,
  `tid` int NOT NULL,
  `content` longtext NOT NULL,
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `quote` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_topic_comment_tid_time` (`tid`, `time`),
  KEY `idx_topic_comment_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_topic_interact_like`;
CREATE TABLE `db_topic_interact_like` (
  `tid` int NOT NULL,
  `uid` int NOT NULL,
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tid`, `uid`),
  KEY `idx_topic_like_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `db_topic_interact_collect`;
CREATE TABLE `db_topic_interact_collect` (
  `tid` int NOT NULL,
  `uid` int NOT NULL,
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tid`, `uid`),
  KEY `idx_topic_collect_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
