--
-- Copyright 2016 Tampere University of Technology, Pori Department
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
-- Server version:               5.5.38-0+wheezy1 - (Debian)
-- Server OS:                    debian-linux-gnu
-- HeidiSQL Version:             8.3.0.4694
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping database structure for ca_frontend
CREATE DATABASE IF NOT EXISTS `ca_frontend` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `ca_frontend`;


-- Dumping structure for table ca_frontend.measurements
CREATE TABLE `measurements` (
  `measurement_id` varchar(40) NOT NULL,
  `backend_id` bigint(20) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`measurement_id`),
  KEY `backend_id_INDEX` (`backend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.measurements_tasks
CREATE TABLE `measurements_tasks` (
  `measurement_id` varchar(40) NOT NULL,
  `task_id` varchar(40) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `measurement_id_task_id_UNIQUE` (`measurement_id`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.measurements_tasks_conditions
CREATE TABLE `measurements_tasks_conditions` (
  `task_id` varchar(40) NOT NULL,
  `condition_id` varchar(40) NOT NULL,
  `condition_key` varchar(40) NOT NULL,
  `condition_value` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  KEY `task_id_INDEX` (`task_id`),
  KEY `condition_id_INDEX` (`condition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.measurements_tasks_ouputs
CREATE TABLE `measurements_tasks_outputs` (
  `task_id` varchar(40) NOT NULL,
  `feature` varchar(40) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `task_id_feature_UNIQUE` (`task_id`,`feature`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.measurements_files
CREATE TABLE IF NOT EXISTS `measurements_files` (
  `guid` varchar(40) NOT NULL,
  `backend_id` bigint(20) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `guid_backend_id_UNIQUE` (`guid`,`backend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.alerts_groups
CREATE TABLE `alerts_groups` (
  `alert_group_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(2040) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`alert_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.alerts_groups_users
CREATE TABLE `alerts_groups_users` (
  `alert_group_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `permission` int(11) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  KEY `alert_group_id_INDEX` (`alert_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.shock_measurements
CREATE TABLE `shock_measurements` (
  `measurement_id` VARCHAR(40) NOT NULL,
  `data_visibility` INT(11) NOT NULL,
  `level` INT(11) NULL DEFAULT NULL,
  `user_id` BIGINT(20) NOT NULL,
  `timestamp` DATETIME NOT NULL,
  `row_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`measurement_id`),
  INDEX `user_id_INDEX` (`user_id`),
  INDEX `data_visibility_INDEX` (`data_visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.

-- Dumping structure for table ca_frontend.shock_accelerometer_data
CREATE TABLE `shock_accelerometer_data` (
  `measurement_id` VARCHAR(40) NOT NULL,
  `x_acc` DOUBLE NOT NULL,
  `y_acc` DOUBLE NOT NULL,
  `z_acc` DOUBLE NOT NULL,
  `systematic_error` DOUBLE DEFAULT NULL,
  `timestamp` DATETIME DEFAULT NULL,
  `row_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`measurement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.

-- Dumping structure for table ca_frontend.shock_location_data
CREATE TABLE `shock_location_data` (
  `measurement_id` VARCHAR(40) NOT NULL,
  `latitude` DOUBLE NOT NULL,
  `longitude` DOUBLE NOT NULL,
  `heading` DOUBLE DEFAULT NULL,
  `speed` DOUBLE DEFAULT NULL,
  `timestamp` DATETIME DEFAULT NULL,
  `row_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`measurement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
