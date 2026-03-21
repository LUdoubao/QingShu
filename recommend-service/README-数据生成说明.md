# 推荐系统模拟数据生成说明

## 概述
本 SQL 脚本用于生成推荐系统所需的模拟测试数据，支持推荐算法的正常执行和验证。

## 数据范围

### 用户 ID 范围
- **用户 ID**: 100000 - 109999 (共 10,000 个用户)
- **用户身份标识**: `user_100000` - `user_109999`

### 内容 ID 范围
- **Quote 表 ID**: 1 - 345577 (共 345,577 条内容)
- 基于现有的 quote 表内容 ID 范围

### 数据量
每张表生成 **100,000 条** 模拟数据

## 生成的数据表

### 1. content_feature_snapshot (内容特征快照表)
- **数据量**: 345,577 条（覆盖所有 quote 内容）
- **用途**: 存储内容的各项特征，用于推荐算法的召回和排序
- **包含字段**:
  - `content_id`: 内容 ID (1-345577)
  - `title`: 诗词标题
  - `content`: 诗词内容
  - `author`: 作者名称（随机从 20 位著名诗人中选择）
  - `source`: 内容来源（唐诗三百首、宋词精选等）
  - `dynasty`: 朝代信息（唐代、宋代等 10 个朝代）
  - `poetry_category`: 诗词分类（唐诗、宋词等 8 个分类）
  - `author_id`: 作者 ID
  - `tag_names`: 标签名称（逗号分隔）
  - `topic_ids`: 话题 ID（逗号分隔）
  - `hot_score`: 热度分数
  - `quality_score`: 质量分数
  - `freshness_score`: 新鲜度分数

### 2. user_profile_snapshot (用户画像快照表)
- **数据量**: 10,000 条（每个用户一条画像记录）
- **用途**: 存储用户的兴趣偏好画像，支持个性化推荐
- **包含字段**:
  - `user_identity`: 用户身份标识 (user_100000 - user_109999)
  - `user_id`: 用户 ID (100000 - 109999)
  - `tag_profile`: 标签偏好权重（JSON 格式）
  - `topic_profile`: 话题偏好权重（JSON 格式）
  - `author_profile`: 作者偏好权重（JSON 格式）
  - `dynasty_profile`: 朝代偏好权重（JSON 格式）
  - `category_profile`: 分类偏好权重（JSON 格式）
  - `recent_content_ids`: 最近浏览的内容 ID 列表（JSON 数组）
  - `last_active_time`: 最后活跃时间（随机在过去 30 天内）

### 3. user_behavior_event (用户行为事件表)
- **数据量**: 100,000 条
- **用途**: 记录用户的行为事件，用于推荐算法的训练和分析
- **包含字段**:
  - `user_identity`: 用户身份标识
  - `user_id`: 用户 ID (100000 - 109999)
  - `content_id`: 内容 ID (1 - 345577)
  - `scene`: 场景标识（home/detail/topic/author/guess）
  - `action_type`: 行为类型（view/like/favorite/comment/share）
  - `action_value`: 行为数值（1-5）
  - `duration`: 停留时长（秒，10-310 秒）
  - `extra_json`: 扩展信息（设备来源、版本等）
  - `created_time`: 创建时间（随机在过去 7 天内）

### 4. recommend_result_cache (推荐结果缓存表)
- **数据量**: 100,000 条
- **用途**: 缓存推荐算法的计算结果，提高推荐响应速度
- **包含字段**:
  - `user_identity`: 用户身份标识
  - `scene`: 推荐场景（home/detail/topic/author/guess）
  - `content_ids`: 推荐的内容 ID 列表（JSON 数组，包含 20 个内容 ID）
  - `algo_version`: 算法版本号（v1.x - v2.x）
  - `created_time`: 创建时间（随机在过去 7 天内）

## 使用方法

### 方式一：MySQL 命令行
```bash
mysql -u your_username -p your_database < generate-recommend-data.sql
```

### 方式二：MySQL Workbench
1. 打开 MySQL Workbench
2. 连接到数据库
3. 选择目标数据库
4. 打开 `generate-recommend-data.sql` 文件
5. 执行整个脚本

### 方式三：其他数据库客户端
在任何支持 MySQL 的客户端工具中打开并执行该 SQL 脚本即可。

## 注意事项

### 1. 性能优化
- 脚本使用 `INSERT INTO ... SELECT` 批量插入，性能较好
- 使用 `ON DUPLICATE KEY UPDATE` 避免主键冲突
- 临时关闭外键检查以提高插入性能

### 2. 数据清理
如果需要重新生成数据，可以先执行以下命令清空表：
```sql
TRUNCATE TABLE content_feature_snapshot;
TRUNCATE TABLE user_profile_snapshot;
TRUNCATE TABLE user_behavior_event;
TRUNCATE TABLE recommend_result_cache;
```

或者在脚本开头取消对应 TRUNCATE 语句的注释。

### 3. 执行时间
根据服务器性能，预计执行时间：
- content_feature_snapshot: 约 30-60 秒
- user_profile_snapshot: 约 10-20 秒
- user_behavior_event: 约 20-40 秒
- recommend_result_cache: 约 20-40 秒

总执行时间约 **1.5 - 3 分钟**

### 4. 磁盘空间
确保数据库有足够的磁盘空间存储这些数据。
预估总数据量：约 50MB - 100MB

## 数据验证

脚本执行完成后，会输出各表的数据量统计：

```sql
SELECT 'content_feature_snapshot' AS table_name, COUNT(*) AS row_count FROM content_feature_snapshot
UNION ALL
SELECT 'user_profile_snapshot', COUNT(*) FROM user_profile_snapshot
UNION ALL
SELECT 'user_behavior_event', COUNT(*) FROM user_behavior_event
UNION ALL
SELECT 'recommend_result_cache', COUNT(*) FROM recommend_result_cache;
```

预期结果：
- content_feature_snapshot: 345,577 条
- user_profile_snapshot: 10,000 条
- user_behavior_event: 100,000 条
- recommend_result_cache: 100,000 条

## 示例查询

### 查询某个用户的画像
```sql
SELECT * FROM user_profile_snapshot 
WHERE user_identity = 'user_100000';
```

### 查询某内容特征的详情
```sql
SELECT * FROM content_feature_snapshot 
WHERE content_id = 1000;
```

### 查询某用户的行为事件
```sql
SELECT * FROM user_behavior_event 
WHERE user_id = 100000
ORDER BY created_time DESC
LIMIT 20;
```

### 查询某用户的推荐缓存
```sql
SELECT * FROM recommend_result_cache 
WHERE user_identity = 'user_100000'
  AND scene = 'home'
ORDER BY created_time DESC
LIMIT 10;
```

## 常见问题

### Q1: 执行时报错 "Packet too large"
**解决方案**: 修改 MySQL 配置，增加 `max_allowed_packet` 参数值。

### Q2: 执行速度慢
**解决方案**: 
1. 确保数据库服务器资源充足
2. 可以在业务低峰期执行
3. 考虑分批插入数据

### Q3: 数据重复
**解决方案**: 脚本已使用 `ON DUPLICATE KEY UPDATE` 处理主键冲突，如需完全重新生成，可先执行 TRUNCATE。

## 技术联系

如有问题，请联系开发团队或查看相关文档：
- 推荐服务时序图：`时序图.md`
- 推荐服务配置：`application.yml`
