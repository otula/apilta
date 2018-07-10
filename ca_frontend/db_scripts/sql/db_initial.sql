--
-- Copyright 2015 Tampere University of Technology, Pori Department
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--   http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- --------------------------------------------------------
-- Host:                         otula.pori.tut.fi
-- Server version:               5.5.50-0+deb8u1 - (Debian)
-- Server OS:                    debian-linux-gnu
-- HeidiSQL Version:             9.3.0.4984
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping database structure for ca_frontend
CREATE DATABASE IF NOT EXISTS `ca_frontend` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `ca_frontend`;


-- Dumping structure for table ca_frontend.backends
CREATE TABLE IF NOT EXISTS `backends` (
  `backend_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `analysis_uri` varchar(2048) DEFAULT NULL,
  `enabled` tinyint(4) NOT NULL DEFAULT '0',
  `description` varchar(1024) DEFAULT NULL,
  `default_task_datagroups` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`backend_id`),
  KEY `user_id_INDEX` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.backends_capabilities
CREATE TABLE IF NOT EXISTS `backends_capabilities` (
  `backend_id` bigint(20) NOT NULL,
  `capability` varchar(45) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `backend_id_capability_UNIQUE` (`backend_id`,`capability`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.backend_groups
CREATE TABLE IF NOT EXISTS `backend_groups` (
  `backend_group_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `description` varchar(2048) DEFAULT NULL,
  `read_public` tinyint(4) NOT NULL,
  `task_public` tinyint(4) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`backend_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.backend_groups_backends
CREATE TABLE IF NOT EXISTS `backend_groups_backends` (
  `backend_group_id` bigint(20) NOT NULL,
  `backend_id` bigint(20) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `backend_group_id_backend_id_UNIQUE` (`backend_group_id`,`backend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.backend_groups_permissions
CREATE TABLE IF NOT EXISTS `backend_groups_permissions` (
  `backend_group_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `permission` int(11) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `backend_group_id_user_id_permission_UNIQUE` (`backend_group_id`,`user_id`,`permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.groups
CREATE TABLE IF NOT EXISTS `groups` (
  `group_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.groups_permissions
CREATE TABLE IF NOT EXISTS `groups_permissions` (
  `group_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `permission` int(11) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `group_id_user_id_permission_UNIQUE` (`group_id`,`user_id`,`permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.tasks
CREATE TABLE IF NOT EXISTS `tasks` (
  `task_id` varchar(40) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `dao_class` varchar(255) NOT NULL,
  `data_visibility` int(11) NOT NULL DEFAULT 0,
  `description` varchar(2048) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `state` int(11) NOT NULL DEFAULT 0,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.tasks_backends
CREATE TABLE IF NOT EXISTS `tasks_backends` (
  `task_id` varchar(40) NOT NULL,
  `backend_id` bigint(20) NOT NULL,
  `status` int(11) NOT NULL,
  `message` varchar(1024) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `task_id_backend_id_UNIQUE` (`task_id`,`backend_id`),
  KEY `status_INDEX` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.tasks_types
CREATE TABLE `tasks_types` (
  `task_id` varchar(40) NOT NULL,
  `task_type` varchar(45) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `task_id_task_type_UNIQUE` (`task_id`,`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_external_ids
CREATE TABLE IF NOT EXISTS `users_external_ids` (
  `user_id` bigint(20) NOT NULL,
  `user_service_id` int(11) NOT NULL,
  `external_id` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `user_id_google_id_UNIQUE` (`external_id`,`user_service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_roles
CREATE TABLE IF NOT EXISTS `users_roles` (
  `user_id` bigint(20) NOT NULL,
  `role` varchar(45) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  KEY `user_id_INDEX` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
