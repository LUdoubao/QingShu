CREATE TABLE IF NOT EXISTS `search_doc_index` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `biz_type` VARCHAR(32) NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `title` VARCHAR(255) DEFAULT NULL,
  `content` TEXT,
  `author_name` VARCHAR(128) DEFAULT NULL,
  `source` VARCHAR(255) DEFAULT NULL,
  `category_id` BIGINT DEFAULT NULL,
  `category_name` VARCHAR(128) DEFAULT NULL,
  `tag_names_text` VARCHAR(1000) DEFAULT NULL,
  `search_text` TEXT,
  `search_text_normalized` TEXT,
  `status` TINYINT NOT NULL DEFAULT 1,
  `is_original` TINYINT NOT NULL DEFAULT 0,
  `publish_time` DATETIME DEFAULT NULL,
  `view_count` BIGINT NOT NULL DEFAULT 0,
  `like_count` BIGINT NOT NULL DEFAULT 0,
  `comment_count` BIGINT NOT NULL DEFAULT 0,
  `favorite_count` BIGINT NOT NULL DEFAULT 0,
  `hot_score` DECIMAL(12,4) NOT NULL DEFAULT 0,
  `quality_score` DECIMAL(12,4) NOT NULL DEFAULT 0,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_type_biz_id` (`biz_type`, `biz_id`),
  KEY `idx_status_deleted_publish` (`status`, `is_deleted`, `updated_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `search_term_index` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `biz_type` VARCHAR(32) NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `term` VARCHAR(128) NOT NULL,
  `term_normalized` VARCHAR(128) NOT NULL,
  `term_type` VARCHAR(32) NOT NULL,
  `source_field` VARCHAR(32) NOT NULL,
  `weight` INT NOT NULL DEFAULT 1,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_term_type_field` (`biz_type`, `biz_id`, `term_normalized`, `term_type`, `source_field`),
  KEY `idx_term_type_biz` (`term_normalized`, `term_type`, `biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `search_suggest_term` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `term_text` VARCHAR(128) NOT NULL,
  `term_normalized` VARCHAR(128) NOT NULL,
  `prefix_text` VARCHAR(64) NOT NULL,
  `term_type` VARCHAR(32) NOT NULL,
  `source_id` BIGINT DEFAULT NULL,
  `source_biz_type` VARCHAR(32) DEFAULT NULL,
  `hot_score` DECIMAL(12,4) NOT NULL DEFAULT 0,
  `quality_score` DECIMAL(12,4) NOT NULL DEFAULT 0,
  `search_count` BIGINT NOT NULL DEFAULT 0,
  `click_count` BIGINT NOT NULL DEFAULT 0,
  `result_count` BIGINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_term_type_source` (`term_normalized`, `term_type`, `source_id`),
  KEY `idx_prefix_type_hot` (`prefix_text`, `term_type`, `hot_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `search_query_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `query_text` VARCHAR(255) NOT NULL,
  `query_text_normalized` VARCHAR(255) NOT NULL,
  `search_count` BIGINT NOT NULL DEFAULT 0,
  `result_count_total` BIGINT NOT NULL DEFAULT 0,
  `click_count` BIGINT NOT NULL DEFAULT 0,
  `last_result_count` INT NOT NULL DEFAULT 0,
  `last_search_time` DATETIME DEFAULT NULL,
  `trend_score` DECIMAL(12,4) NOT NULL DEFAULT 0,
  `hot_score` DECIMAL(12,4) NOT NULL DEFAULT 0,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_query_normalized` (`query_text_normalized`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_search_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `query_text` VARCHAR(255) NOT NULL,
  `query_text_normalized` VARCHAR(255) NOT NULL,
  `search_count` INT NOT NULL DEFAULT 1,
  `last_search_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_query` (`user_id`, `query_text_normalized`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `search_index_sync_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `biz_type` VARCHAR(32) NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `op_type` VARCHAR(32) NOT NULL,
  `task_status` VARCHAR(32) NOT NULL DEFAULT 'INIT',
  `retry_count` INT NOT NULL DEFAULT 0,
  `fail_reason` VARCHAR(500) DEFAULT NULL,
  `next_retry_time` DATETIME DEFAULT NULL,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_retry` (`task_status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
