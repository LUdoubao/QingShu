-- 创建话题相关表的SQL脚本

-- 话题主表（topic）- 存储话题核心信息
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题ID（主键）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '话题名称（唯一，格式建议带#，如#治愈文案#）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '话题描述（支持富文本）',
  `cover_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '话题封面图KEY（关联文件存储）',
  `creator_id` bigint UNSIGNED NOT NULL COMMENT '创建人ID（关联user表）',
  `category_id` bigint NOT NULL COMMENT '话题分类ID（关联topic_category表）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-审核中 1-已发布 2-屏蔽 3-草稿 4-未通过 5-下架（对齐quote表状态）',
  `is_recommend` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否官方推荐：0-否 1-是',
  `weight` int NOT NULL DEFAULT 0 COMMENT '排序权重（值越大越靠前，用于推荐排序）',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE COMMENT '话题名称唯一，避免重复',
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE COMMENT '查询用户创建的话题',
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE COMMENT '按分类筛选话题',
  INDEX `idx_status_recommend`(`status` ASC, `is_recommend` ASC, `weight` DESC) USING BTREE COMMENT '推荐话题查询',
  FULLTEXT INDEX `idx_name_desc`(`name`, `description`) COMMENT '话题搜索（名称+描述）'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题主表' ROW_FORMAT = DYNAMIC;

-- 话题分类表（topic_category）- 支持多级分类，便于筛选
DROP TABLE IF EXISTS `topic_category`;
CREATE TABLE `topic_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID（主键）',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID：0=一级分类，支持多级嵌套',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类说明',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重：值越小越靠前',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE COMMENT '查询子分类',
  INDEX `idx_status_sort`(`status` ASC, `sort` ASC) USING BTREE COMMENT '筛选启用的分类并排序'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题分类表（支持多级）' ROW_FORMAT = DYNAMIC;

-- 文案-话题关联表（quote_topic）- 多对多关联，灵活绑定
DROP TABLE IF EXISTS `quote_topic`;
CREATE TABLE `quote_topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID（主键）',
  `quote_id` bigint NOT NULL COMMENT '文案ID（关联quote表）',
  `topic_id` bigint NOT NULL COMMENT '话题ID（关联topic表）',
  `bind_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `binder_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '绑定人ID（用户/管理员）',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-有效 1-已解除绑定',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quote_topic`(`quote_id` ASC, `topic_id` ASC) USING BTREE COMMENT '避免文案重复绑定同一话题',
  INDEX `idx_quote_id`(`quote_id` ASC) USING BTREE COMMENT '查询文案关联的所有话题',
  INDEX `idx_topic_id`(`topic_id` ASC, `bind_time` DESC) USING BTREE COMMENT '查询话题下的所有文案（按时间排序）'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文案与话题的多对多关联表' ROW_FORMAT = DYNAMIC;

-- 用户-话题关注表（user_topic_follow）- 记录用户关注关系，支撑动态推送
DROP TABLE IF EXISTS `user_topic_follow`;
CREATE TABLE `user_topic_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关注ID（主键）',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID（关联user表）',
  `topic_id` bigint NOT NULL COMMENT '话题ID（关联topic表）',
  `follow_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `unfollow_time` datetime NULL DEFAULT NULL COMMENT '取消关注时间（NULL=未取消）',
  `is_valid` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否有效：0-已取消 1-有效',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_topic`(`user_id` ASC, `topic_id` ASC) USING BTREE COMMENT '避免用户重复关注同一话题',
  INDEX `idx_user_id`(`user_id` ASC, `is_valid` ASC) USING BTREE COMMENT '查询用户关注的所有话题',
  INDEX `idx_topic_id`(`topic_id` ASC, `is_valid` ASC) USING BTREE COMMENT '查询话题的所有关注用户'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注话题表' ROW_FORMAT = DYNAMIC;

-- 话题-标签关联表（topic_tag）- 复用现有标签体系，增强检索
DROP TABLE IF EXISTS `topic_tag`;
CREATE TABLE `topic_tag`  (
  `topic_id` bigint NOT NULL COMMENT '话题ID（关联topic表）',
  `tag_id` bigint NOT NULL COMMENT '标签ID（关联现有tag表）',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联时间',
  PRIMARY KEY (`topic_id`, `tag_id`) USING BTREE COMMENT '联合主键，避免重复关联',
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE COMMENT '查询标签关联的所有话题'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题与标签的多对多关联表' ROW_FORMAT = DYNAMIC;

-- 话题统计表（topic_statistics）- 冗余统计数据，提升查询性能
DROP TABLE IF EXISTS `topic_statistics`;
CREATE TABLE `topic_statistics`  (
  `topic_id` bigint NOT NULL COMMENT '话题ID（主键，关联topic表）',
  `quote_count` int NOT NULL DEFAULT 0 COMMENT '关联文案总数',
  `active_user_count` int NOT NULL DEFAULT 0 COMMENT '参与用户数（发布文案的独立用户数）',
  `follow_count` int NOT NULL DEFAULT 0 COMMENT '关注用户总数',
  `view_count` bigint NOT NULL DEFAULT 0 COMMENT '话题总浏览量',
  `today_quote_count` int NOT NULL DEFAULT 0 COMMENT '今日新增文案数',
  `hot_quote_id` bigint NULL DEFAULT NULL COMMENT '热门文案ID（点赞数最高，便于快速展示）',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计更新时间',
  PRIMARY KEY (`topic_id`) USING BTREE,
  INDEX `idx_hot`(`quote_count` DESC, `follow_count` DESC) USING BTREE COMMENT '热门话题排序查询'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题统计信息表（冗余存储，避免聚合计算）' ROW_FORMAT = DYNAMIC;

-- 话题审核日志表（topic_audit_log）- 支撑内容合规，追溯审核记录
DROP TABLE IF EXISTS `topic_audit_log`;
CREATE TABLE `topic_audit_log`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID（主键）',
  `topic_id` bigint NOT NULL COMMENT '话题ID（关联topic表）',
  `audit_status` tinyint NOT NULL COMMENT '审核结果：0-待审核 1-通过 2-拒绝',
  `audit_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核理由',
  `auditor_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '审核员ID（关联user表，管理员角色）',
  `audit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_topic_id`(`topic_id` ASC) USING BTREE COMMENT '查询话题的所有审核记录',
  INDEX `idx_audit_time`(`audit_time` ASC) USING BTREE COMMENT '按时间追溯审核记录'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题审核日志表' ROW_FORMAT = DYNAMIC;

-- 话题管理员表（topic_manager）- 支持多管理员协作管理
DROP TABLE IF EXISTS `topic_manager`;
CREATE TABLE `topic_manager`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `topic_id` bigint NOT NULL COMMENT '话题ID（关联topic表）',
  `manager_id` bigint UNSIGNED NOT NULL COMMENT '管理员ID（关联user表，支持普通用户成为管理员）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE = utf8mb4_0900_ai_ci NOT NULL DEFAULT 'EDITOR' COMMENT '管理角色：ADMIN-超级管理员 EDITOR-内容编辑',
  `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `remove_time` datetime NULL DEFAULT NULL COMMENT '移除时间（NULL=有效）',
  `is_valid` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否有效：0-已移除 1-有效',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_topic_manager`(`topic_id` ASC, `manager_id` ASC) USING BTREE COMMENT '避免重复添加同一管理员',
  INDEX `idx_manager_id`(`manager_id` ASC) USING BTREE COMMENT '查询管理员负责的所有话题'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题管理员表（支持多管理员）' ROW_FORMAT = DYNAMIC;

-- 话题动态表（topic_timeline）- 推模式存储话题动态，支撑信息流
DROP TABLE IF EXISTS `topic_timeline`;
CREATE TABLE `topic_timeline`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `topic_id` bigint NOT NULL COMMENT '话题ID（关联topic表）',
  `event_type` tinyint NOT NULL COMMENT '事件类型：1-新增文案 2-热门文案 3-官方推荐 4-话题更新',
  `target_id` bigint NOT NULL COMMENT '目标ID（如文案ID、话题ID）',
  `actor_id` bigint UNSIGNED NOT NULL COMMENT '事件发起者ID（用户/管理员）',
  `event_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件发生时间',
  `weight` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '动态权重（用于排序）',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-有效 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_topic_time`(`topic_id` ASC, `event_time` DESC, `weight` DESC) USING BTREE COMMENT '查询话题动态（按时间+权重排序）',
  INDEX `idx_event_type`(`event_type` ASC) USING BTREE COMMENT '筛选特定类型的动态'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题动态表（推模式，支撑信息流）' ROW_FORMAT = DYNAMIC;

-- 插入一些默认数据
-- 插入默认分类
INSERT INTO `topic_category` (`id`, `parent_id`, `name`, `description`, `sort`, `status`) VALUES
(1, 0, '情感', '关于情感生活的话题', 1, 1),
(2, 0, '励志', '激励人心的话题', 2, 1),
(3, 0, '生活', '日常生活相关话题', 3, 1),
(4, 1, '爱情', '关于爱情的情感话题', 1, 1),
(5, 1, '友情', '关于友情的情感话题', 2, 1);