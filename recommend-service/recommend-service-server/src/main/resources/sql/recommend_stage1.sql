-- Stage1 supplemental tables for recommendation system

CREATE TABLE IF NOT EXISTS user_behavior_event (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_identity VARCHAR(64) NOT NULL COMMENT '登录用户ID或游客标识',
  user_id BIGINT NULL DEFAULT NULL COMMENT '登录用户ID',
  content_id BIGINT NOT NULL COMMENT '内容ID',
  scene VARCHAR(32) NOT NULL COMMENT '场景：首页/详情页/搜索页/话题页',
  action_type VARCHAR(32) NOT NULL COMMENT 'view/like/favorite/comment/share',
  action_value INT NOT NULL DEFAULT 1 COMMENT '行为值',
  duration INT NOT NULL DEFAULT 0 COMMENT '停留时长（秒）',
  extra JSON NULL COMMENT '扩展字段',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_time(user_identity, created_time),
  INDEX idx_content_time(content_id, created_time),
  INDEX idx_action_time(action_type, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS content_feature_snapshot (
  content_id BIGINT NOT NULL PRIMARY KEY,
  title VARCHAR(255) NULL,
  content TEXT NULL,
  author VARCHAR(255) NULL,
  source VARCHAR(255) NULL,
  dynasty VARCHAR(32) NULL,
  poetry_category VARCHAR(32) NULL,
  author_id BIGINT NULL,
  tag_names TEXT NULL,
  topic_ids TEXT NULL,
  hot_score DOUBLE DEFAULT 0,
  quality_score DOUBLE DEFAULT 0,
  freshness_score DOUBLE DEFAULT 0,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_profile_snapshot (
  user_identity VARCHAR(64) NOT NULL PRIMARY KEY,
  user_id BIGINT NULL DEFAULT NULL,
  tag_profile JSON NULL COMMENT '偏好标签权重',
  topic_profile JSON NULL COMMENT '偏好话题权重',
  author_profile JSON NULL COMMENT '偏好作者权重',
  dynasty_profile JSON NULL COMMENT '偏好朝代权重',
  category_profile JSON NULL COMMENT '偏好分类权重',
  recent_content_ids JSON NULL COMMENT '最近浏览内容ID',
  last_active_time DATETIME NULL,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recommend_result_cache (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_identity VARCHAR(64) NOT NULL,
  scene VARCHAR(32) NOT NULL,
  content_ids JSON NOT NULL,
  algo_version VARCHAR(32) NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_scene(user_identity, scene),
  INDEX idx_created_time(created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Hot score example SQL
-- SELECT content_id,
--        SUM(CASE action_type WHEN 'view' THEN 1 WHEN 'like' THEN 3 WHEN 'favorite' THEN 5 WHEN 'comment' THEN 6 WHEN 'share' THEN 8 ELSE 1 END) AS score
-- FROM user_behavior_event
-- WHERE created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
-- GROUP BY content_id
-- ORDER BY score DESC
-- LIMIT 200;
