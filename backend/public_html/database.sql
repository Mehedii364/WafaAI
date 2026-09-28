-- Wafa AI Production Database Schema
-- Compatible with MySQL 8.x / MariaDB / InfinityFree phpMyAdmin

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- Table: users (Optional for authenticated multi-user mode)
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(64) NOT NULL UNIQUE,
  `email` VARCHAR(191) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `role` ENUM('admin', 'user') DEFAULT 'user',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: api_key_vault (For server-side encrypted key rotation)
CREATE TABLE IF NOT EXISTS `api_key_vault` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `slot_index` TINYINT UNSIGNED NOT NULL,
  `label` VARCHAR(64) NOT NULL,
  `masked_key` VARCHAR(64) NOT NULL,
  `encrypted_key` TEXT NOT NULL,
  `status` ENUM('AVAILABLE', 'ACTIVE', 'COOLDOWN', 'RATE_LIMITED', 'INVALID', 'ERROR') DEFAULT 'AVAILABLE',
  `request_count` INT UNSIGNED DEFAULT 0,
  `error_count` INT UNSIGNED DEFAULT 0,
  `cooldown_until` DATETIME NULL,
  `last_used_at` DATETIME NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_slot` (`slot_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: audit_logs
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `action` VARCHAR(64) NOT NULL,
  `ip_address` VARCHAR(45) NOT NULL,
  `status_code` SMALLINT UNSIGNED NOT NULL,
  `message` VARCHAR(255) NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_action_created` (`action`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;
