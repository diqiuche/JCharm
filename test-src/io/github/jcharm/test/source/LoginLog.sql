/*
Navicat MySQL Data Transfer

Source Server         : LinuxMySQL
Source Server Version : 50713
Source Host           : 192.168.1.249:3306
Source Database       : JCharm

Target Server Type    : MYSQL
Target Server Version : 50713
File Encoding         : 65001

Date: 2016-07-26 22:21:15
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for LoginLog
-- ----------------------------
DROP TABLE IF EXISTS `LoginLog`;
CREATE TABLE `LoginLog` (
  `sessionId` int(11) NOT NULL COMMENT '登陆会话ID',
  `userId` int(11) NOT NULL COMMENT '登陆用户ID',
  `loginAgent` varchar(128) NOT NULL COMMENT '登陆端信息',
  `loginIP` varchar(32) NOT NULL COMMENT '登陆IP',
  `loginTime` datetime NOT NULL COMMENT '登陆时间',
  `logoutTime` datetime NOT NULL COMMENT '注销时间',
  PRIMARY KEY (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
