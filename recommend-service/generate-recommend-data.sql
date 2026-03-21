-- =====================================================
-- 推荐系统模拟数据生成脚本
-- 用户 ID 范围：100000-109999 (共 10000 个用户)
-- Quote 表 ID 范围：1-345577
-- 每张表生成 10 万条模拟数据
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 生成 content_feature_snapshot 表数据 (345577 条)
-- =====================================================

-- 删除旧数据（如果需要）
TRUNCATE TABLE content_feature_snapshot;

-- 创建临时表存储序列号
DROP TEMPORARY TABLE IF EXISTS temp_numbers;
CREATE TEMPORARY TABLE temp_numbers (
    id INT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

-- 插入序列号 1 到 345577
INSERT INTO temp_numbers (id)
SELECT @row := @row + 1 AS id
FROM (
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t1,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t2,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t3,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t4,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t5,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t6,
(SELECT @row := 0) r
LIMIT 345577;

-- 插入内容特征数据
INSERT INTO content_feature_snapshot (
    content_id, title, content, author, source, dynasty, 
    poetry_category, author_id, tag_names, topic_ids, 
    hot_score, quality_score, freshness_score
)
SELECT 
    id AS content_id,
    CONCAT('诗词标题_', id) AS title,
    CONCAT('这是诗词内容正文，描述了一些优美的自然景色和人文情怀。ID:', id) AS content,
    ELT(FLOOR(1 + RAND() * 20), 
        '李白', '杜甫', '白居易', '王维', '孟浩然', '苏轼', '辛弃疾', '李清照',
        '陆游', '杨万里', '范成大', '欧阳修', '王安石', '曾巩', '柳宗元', '韩愈',
        '刘禹锡', '李商隐', '杜牧', '岑参') AS author,
    ELT(FLOOR(1 + RAND() * 5), '唐诗三百首', '宋词精选', '元曲赏析', '古文观止', '诗经') AS source,
    ELT(FLOOR(1 + RAND() * 10), 
        '唐代', '宋代', '元代', '明代', '清代', '先秦', '汉代', '魏晋', '南北朝', '隋代') AS dynasty,
    ELT(FLOOR(1 + RAND() * 8), 
        '唐诗', '宋词', '元曲', '律诗', '绝句', '古风', '乐府', '词牌') AS poetry_category,
    FLOOR(1 + RAND() * 1000) AS author_id,
    ELT(FLOOR(1 + RAND() * 10),
        '山水，田园，自然', '抒情，咏志，人生', '边塞，战争，思乡', '友情，送别，思念',
        '爱情，相思，离别', '咏史，怀古，感慨', '写景，四季，风光', '哲理，感悟，修身',
        '饮酒，品茶，闲适', '咏物，花鸟，虫鱼') AS tag_names,
    CONCAT(
        FLOOR(1 + RAND() * 100), ',',
        FLOOR(1 + RAND() * 100), ',',
        FLOOR(1 + RAND() * 100)
    ) AS topic_ids,
    ROUND(RAND() * 100, 2) AS hot_score,
    ROUND(RAND() * 10, 2) AS quality_score,
    ROUND(RAND() * 10, 2) AS freshness_score
FROM temp_numbers
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    author = VALUES(author),
    source = VALUES(source),
    dynasty = VALUES(dynasty),
    poetry_category = VALUES(poetry_category),
    author_id = VALUES(author_id),
    tag_names = VALUES(tag_names),
    topic_ids = VALUES(topic_ids),
    hot_score = VALUES(hot_score),
    quality_score = VALUES(quality_score),
    freshness_score = VALUES(freshness_score);

-- =====================================================
-- 2. 生成 user_profile_snapshot 表数据 (1 万条，用户 ID: 100000-109999)
-- =====================================================

-- 删除旧数据（如果需要）
TRUNCATE TABLE user_profile_snapshot;

-- 重新填充临时表用于生成 10000 个用户
TRUNCATE TABLE temp_numbers;

INSERT INTO temp_numbers (id)
SELECT @row2 := @row2 + 1 AS id
FROM (
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t1,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t2,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t3,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t4,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t5,
(SELECT @row2 := 0) r
LIMIT 10000;

INSERT INTO user_profile_snapshot (
    user_identity, user_id, tag_profile, topic_profile, author_profile,
    dynasty_profile, category_profile, recent_content_ids, last_active_time
)
SELECT 
    CONCAT('user_', 100000 + id - 1) AS user_identity,
    100000 + id - 1 AS user_id,
    -- 标签偏好 JSON
    CONCAT(
        '{"',
        ELT(FLOOR(1 + RAND() * 10), '山水', '田园', '抒情', '边塞', '友情', '爱情', '咏史', '写景', '哲理', '咏物'),
        '": ', ROUND(RAND() * 10, 2),
        ', "',
        ELT(FLOOR(1 + RAND() * 10), '山水', '田园', '抒情', '边塞', '友情', '爱情', '咏史', '写景', '哲理', '咏物'),
        '": ', ROUND(RAND() * 10, 2),
        ', "',
        ELT(FLOOR(1 + RAND() * 10), '山水', '田园', '抒情', '边塞', '友情', '爱情', '咏史', '写景', '哲理', '咏物'),
        '": ', ROUND(RAND() * 10, 2),
        '}'
    ) AS tag_profile,
    -- 话题偏好 JSON
    CONCAT(
        '{"',
        FLOOR(1 + RAND() * 100), '": ', ROUND(RAND() * 10, 2),
        ', "',
        FLOOR(1 + RAND() * 100), '": ', ROUND(RAND() * 10, 2),
        ', "',
        FLOOR(1 + RAND() * 100), '": ', ROUND(RAND() * 10, 2),
        '}'
    ) AS topic_profile,
    -- 作者偏好 JSON
    CONCAT(
        '{"',
        ELT(FLOOR(1 + RAND() * 20), '李白', '杜甫', '白居易', '王维', '孟浩然', '苏轼', '辛弃疾', '李清照',
            '陆游', '杨万里', '范成大', '欧阳修', '王安石', '曾巩', '柳宗元', '韩愈',
            '刘禹锡', '李商隐', '杜牧', '岑参'),
        '": ', ROUND(RAND() * 10, 2),
        ', "',
        ELT(FLOOR(1 + RAND() * 20), '李白', '杜甫', '白居易', '王维', '孟浩然', '苏轼', '辛弃疾', '李清照',
            '陆游', '杨万里', '范成大', '欧阳修', '王安石', '曾巩', '柳宗元', '韩愈',
            '刘禹锡', '李商隐', '杜牧', '岑参'),
        '": ', ROUND(RAND() * 10, 2),
        '}'
    ) AS author_profile,
    -- 朝代偏好 JSON
    CONCAT(
        '{"',
        ELT(FLOOR(1 + RAND() * 10), '唐代', '宋代', '元代', '明代', '清代', '先秦', '汉代', '魏晋', '南北朝', '隋代'),
        '": ', ROUND(RAND() * 10, 2),
        ', "',
        ELT(FLOOR(1 + RAND() * 10), '唐代', '宋代', '元代', '明代', '清代', '先秦', '汉代', '魏晋', '南北朝', '隋代'),
        '": ', ROUND(RAND() * 10, 2),
        '}'
    ) AS dynasty_profile,
    -- 分类偏好 JSON
    CONCAT(
        '{"',
        ELT(FLOOR(1 + RAND() * 8), '唐诗', '宋词', '元曲', '律诗', '绝句', '古风', '乐府', '词牌'),
        '": ', ROUND(RAND() * 10, 2),
        ', "',
        ELT(FLOOR(1 + RAND() * 8), '唐诗', '宋词', '元曲', '律诗', '绝句', '古风', '乐府', '词牌'),
        '": ', ROUND(RAND() * 10, 2),
        '}'
    ) AS category_profile,
    -- 最近浏览内容 ID JSON
    CONCAT(
        '[',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577),
        ']'
    ) AS recent_content_ids,
    -- 最后活跃时间（随机在过去 30 天内）
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) AS last_active_time
FROM temp_numbers;

-- =====================================================
-- 3. 生成 user_behavior_event 表数据 (10 万条)
-- =====================================================

-- 删除旧数据（如果需要）
TRUNCATE TABLE user_behavior_event;

-- 重新填充临时表用于生成 100000 条行为记录
TRUNCATE TABLE temp_numbers;

INSERT INTO temp_numbers (id)
SELECT @row3 := @row3 + 1 AS id
FROM (
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t1,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t2,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t3,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t4,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t5,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t6,
(
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) t7,
(SELECT @row3 := 0) r
LIMIT 100000;

INSERT INTO user_behavior_event (
    user_identity, user_id, content_id, scene, action_type,
    action_value, duration, extra_json, created_time
)
SELECT 
    CONCAT('user_', 100000 + FLOOR(RAND() * 10000)) AS user_identity,
    100000 + FLOOR(RAND() * 10000) AS user_id,
    FLOOR(1 + RAND() * 345577) AS content_id,
    ELT(FLOOR(1 + RAND() * 5), 'home', 'detail', 'topic', 'author', 'guess') AS scene,
    ELT(FLOOR(1 + RAND() * 5), 'view', 'like', 'favorite', 'comment', 'share') AS action_type,
    FLOOR(1 + RAND() * 5) AS action_value,
    FLOOR(10 + RAND() * 300) AS duration,
    CONCAT('{"source": "', ELT(FLOOR(1 + RAND() * 3), 'android', 'ios', 'web'), '", "version": "1.0"}') AS extra_json,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) AS created_time
FROM temp_numbers;

-- =====================================================
-- 4. 生成 recommend_result_cache 表数据 (10 万条)
-- =====================================================

-- 删除旧数据（如果需要）
TRUNCATE TABLE recommend_result_cache;

-- 使用已有的 temp_numbers 临时表 (100000 条记录)

INSERT INTO recommend_result_cache (
    user_identity, scene, content_ids, algo_version, created_time
)
SELECT 
    CONCAT('user_', 100000 + id - 1) AS user_identity,
    ELT(FLOOR(1 + RAND() * 5), 'home', 'detail', 'topic', 'author', 'guess') AS scene,
    -- 生成包含 20 个内容 ID 的 JSON 数组
    CONCAT(
        '[',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577), ',',
        FLOOR(1 + RAND() * 345577),
        ']'
    ) AS content_ids,
    CONCAT('v', FLOOR(1 + RAND() * 3), '.', FLOOR(RAND() * 10)) AS algo_version,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) AS created_time
FROM temp_numbers;

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_numbers;

-- =====================================================
-- 数据验证查询
-- =====================================================

-- 验证各表数据量
SELECT 'content_feature_snapshot' AS table_name, COUNT(*) AS row_count FROM content_feature_snapshot
UNION ALL
SELECT 'user_profile_snapshot', COUNT(*) FROM user_profile_snapshot
UNION ALL
SELECT 'user_behavior_event', COUNT(*) FROM user_behavior_event
UNION ALL
SELECT 'recommend_result_cache', COUNT(*) FROM recommend_result_cache;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 脚本执行完成
-- =====================================================
